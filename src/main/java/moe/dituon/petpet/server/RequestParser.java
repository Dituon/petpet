package moe.dituon.petpet.server;

import kotlin.Pair;

import java.io.InputStream;

public abstract class RequestParser {
    protected Pair<InputStream, String> imagePair;

    public Pair<InputStream, String> getImagePair() {
        return imagePair;
    }

    public void close() {
        imagePair = null;
    }
}

