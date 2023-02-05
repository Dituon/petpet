package moe.dituon.petpet.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import kotlin.jvm.functions.Function0;
import moe.dituon.petpet.share.GifAvatarExtraDataProvider;
import moe.dituon.petpet.share.ImageSynthesisCore;
import moe.dituon.petpet.share.TextExtraData;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FormDataParser extends RequestParser {
    private final Map<String, MultiPart> map;

    public FormDataParser(ServerPetService service, HttpExchange httpExchange) throws IOException, IllegalArgumentException {
        Headers headers = httpExchange.getRequestHeaders();
        String contentType = headers.getFirst("Content-Type");

        //found form data
        String boundary = contentType.substring(contentType.indexOf("boundary=") + 9);
        // as of rfc7578 - prepend "\r\n--"
        byte[] boundaryBytes = ("\r\n--" + boundary).getBytes(StandardCharsets.UTF_8);
        InputStream payloadStream = httpExchange.getRequestBody();
        byte[] payload;
        try (payloadStream) {
            payload = payloadStream.readAllBytes();
        }

        map = new HashMap<>(6);

        List<Integer> offsets = searchBytes(payload, boundaryBytes, 0, payload.length - 1);
        offsets.add(0, 0);
        for (int idx = 0; idx < offsets.size(); idx++) {
            int startPart = offsets.get(idx);
            int endPart = payload.length;
            if (idx < offsets.size() - 1) {
                endPart = offsets.get(idx + 1);
            }
            int partLength = endPart - startPart;
            //look for header
            int headerEnd = indexOf(payload, "\r\n\r\n".getBytes(StandardCharsets.UTF_8), startPart, endPart);
            if (headerEnd > 0) {
                MultiPart p = new MultiPart();
                String header = new String(payload, startPart, headerEnd);
                // extract name from header
                int nameIndex = header.indexOf("\r\nContent-Disposition: form-data; name=");
                if (nameIndex >= 0) {
                    int startMarker = nameIndex + 39;
                    //check for extra filename field
                    int fileNameStart = header.indexOf("; filename=");
                    if (fileNameStart >= 0) {
                        p.name = header.substring(startMarker, fileNameStart).replace('"', ' ').trim();
                        p.type = PartType.FILE;
                    } else {
                        int endMarker = header.indexOf("\r\n", startMarker);
                        if (endMarker == -1)
                            endMarker = header.length();
                        p.name = header.substring(startMarker, endMarker).replace('"', ' ').trim();
                        p.type = PartType.TEXT;
                    }
                } else {
                    // skip entry if no name is found
                    continue;
                }
                // extract content type from header
                int typeIndex = header.indexOf("\r\nContent-Type:");
                if (typeIndex >= 0) {
                    int startMarker = typeIndex + 15;
                    int endMarker = header.indexOf("\r\n", startMarker);
                    if (endMarker == -1)
                        endMarker = header.length();
                    p.contentType = header.substring(startMarker, endMarker).trim();
                }

                int bodyLength = partLength - headerEnd - 4;
                if (p.type == PartType.TEXT) {
                    p.value = new String(payload, headerEnd + 4, bodyLength);
                } else {
                    p.stream = new ByteArrayInputStream(payload, headerEnd + 4, bodyLength);
                }
                map.put(p.name, p);
            }
        }

        try {
            String key = map.get("key").value;
            System.out.println(key);
            assert key != null;

            super.imagePair = service.generateImage(
                    key,
                    new GifAvatarExtraDataProvider(
                            getImageListLambda("fromAvatar"),
                            getImageListLambda("toAvatar"),
                            getImageListLambda("groupAvatar"),
                            getImageListLambda("botAvatar"),
                            null
                    ), new TextExtraData(
                            getString("fromName"),
                            getString("toName"),
                            getString("groupName"),
                            map.get("textList") == null ? Collections.emptyList() :
                                    Arrays.asList(map.get("textList").value.split(" "))
                    ), null
            );
        } catch (AssertionError e) {
            throw new IllegalArgumentException();
        }
    }

    public String getString(String key) {
        MultiPart part = map.get(key);
        return part == null ? key : part.value;
    }

    public Function0<List<BufferedImage>> getImageListLambda(String key) {
        return map.get(key) == null ? null : () -> map.get(key).getImageList();
    }

    /**
     * Search bytes in byte array returns indexes within this byte-array of all
     * occurrences of the specified(search bytes) byte array in the specified
     * range
     * borrowed from <a href="https://github.com/riversun/finbin/blob/master/src/main/java/org/riversun/finbin/BinarySearcher.java">...</a>
     *
     * @return result index list
     */
    public List<Integer> searchBytes(byte[] srcBytes, byte[] searchBytes, int searchStartIndex, int searchEndIndex) {
        final int destSize = searchBytes.length;
        final List<Integer> positionIndexList = new ArrayList<>();
        int cursor = searchStartIndex;
        while (cursor < searchEndIndex + 1) {
            int index = indexOf(srcBytes, searchBytes, cursor, searchEndIndex);
            if (index >= 0) {
                positionIndexList.add(index);
                cursor = index + destSize;
            } else {
                cursor++;
            }
        }
        return positionIndexList;
    }

    /**
     * Returns the index within this byte-array of the first occurrence of the
     * specified(search bytes) byte array.<br>
     * Starting the search at the specified index, and end at the specified
     * index.
     * borrowed from <a href="https://github.com/riversun/finbin/blob/master/src/main/java/org/riversun/finbin/BinarySearcher.java">...</a>
     */
    public int indexOf(byte[] srcBytes, byte[] searchBytes, int startIndex, int endIndex) {
        if (searchBytes.length == 0 || (endIndex - startIndex + 1) < searchBytes.length) {
            return -1;
        }
        int maxScanStartPosIdx = srcBytes.length - searchBytes.length;
        final int loopEndIdx = Math.min(endIndex, maxScanStartPosIdx);
        int lastScanIdx = -1;
        label:
        // goto label
        for (int i = startIndex; i <= loopEndIdx; i++) {
            for (int j = 0; j < searchBytes.length; j++) {
                if (srcBytes[i + j] != searchBytes[j]) {
                    continue label;
                }
                lastScanIdx = i + j;
            }
            if (endIndex < lastScanIdx || lastScanIdx - i + 1 < searchBytes.length) {
                return -1;
            }
            return i;
        }
        return -1;
    }

    public static class MultiPart {
        public PartType type;
        public String contentType;
        public String name;
        public String value = null;
        public InputStream stream;

        public List<BufferedImage> getImageList() {
            try {
                return ImageSynthesisCore.getImageAsList(stream);
            } catch (IOException e) {
                return null;
            }
        }
    }

    public enum PartType {
        TEXT, FILE
    }
}