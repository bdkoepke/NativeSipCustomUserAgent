package pw.swordfish.util.function;

public final class Suppliers<T> {
    public static <T> Supplier<T> memoize(final Supplier<T> delegate) {
        return new Supplier<T>() {
            private T instance = null;

            @Override
            public T get() {
                return instance == null ? (instance = delegate.get()) : instance;
            }
        };
    }
}
