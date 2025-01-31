package moe.dituon.petpet.core;

import lombok.Getter;
import lombok.Setter;
import moe.dituon.petpet.core.imgres.ImageResourceManager;
import moe.dituon.petpet.core.imgres.ResourceManagerConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class GlobalContext {
    public static final int API_VERSION = 101;
    @Getter
    @Setter
    protected BaseRenderConfig renderConfig;
    @Getter
    public final List<String> features = new ArrayList<>(4);
    public final FontManager fontManager = FontManager.getInstance();

    public final ImageResourceManager resourceManager = new ImageResourceManager(new ResourceManagerConfig());
    public final Executor imageProcessExecutor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
    );

    /**
     * Executes image processing on a list of elements using a specified bifunction. <br/>
     * This method utilizes multithreading to process each element, ensuring all elements are processed concurrently.
     *
     * @param elements The list of elements to be processed.
     * @param function The bifunction used for processing, which takes the index of the element and the element itself as arguments.
     * @return Returns a list containing the results of processing each element.
     * @param <T> The type of the elements to be processed.
     * @param <R> The type of the processing result.
     */
    @SuppressWarnings("unchecked")
    public <T, R> List<R> execImageProcess(List<T> elements, BiFunction<Integer, T, R> function) {
        int size = elements.size();
        var latch = new CountDownLatch(size);
        R[] result = (R[]) new Object[size];
        for (int i = 0; i < size; i++) {
            var element = elements.get(i);
            int fi = i;
            imageProcessExecutor.execute(() -> {
                result[fi] = function.apply(fi, element);
                latch.countDown();
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
        return Arrays.asList(result);
    }

    /**
     * Executes image processing tasks in parallel using a thread pool.
     * ensuring that all image processing tasks are completed before the main thread continues.
     *
     * @param elements A list of elements to be processed, the specific type depends on the image processing function.
     * @param function A BiConsumer functional interface instance that defines how to process each element.
     * @param <T> A generic type parameter indicating that this method can accept lists of any type.
     */
    public <T> void execImageProcess(List<T> elements, BiConsumer<Integer, T> function) {
        var latch = new CountDownLatch(elements.size());
        for (int i = 0; i < elements.size(); i++) {
            var element = elements.get(i);
            if (element == null) {
                latch.countDown();
                continue;
            }
            int fi = i;
            imageProcessExecutor.execute(() -> {
                function.accept(fi, element);
                latch.countDown();
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private static class GlobalServiceInstance {
        private static final GlobalContext INSTANCE = new GlobalContext();
    }

    public static GlobalContext getInstance() {
        return GlobalServiceInstance.INSTANCE;
    }
}
