package se.chalmers.ju2jmh.testinput;

public class NestedClasses {

    public AbstractInner getAbstractInner() {
        return new AbstractInner() {};
    }

    public static StaticAbstract getStaticAbstract() {
        return new StaticAbstract() {};
    }

    public interface Interface {}

    public class Inner implements Interface {}

    public abstract class AbstractInner implements Interface {}

    public static class Static implements Interface {}

    public static abstract class StaticAbstract implements Interface {}

    public class InnerWithInner implements Interface {
        public class InnerInner extends AbstractInner {}
    }

    public static class StaticWithStatic implements Interface {
        public static class StaticStatic extends StaticAbstract {}
    }
}
