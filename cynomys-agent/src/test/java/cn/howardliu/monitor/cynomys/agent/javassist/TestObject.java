package cn.howardliu.monitor.cynomys.agent.javassist;

/**
 * <br>created at 17-8-22
 *
 * @author liuxh
 * @since 0.0.1
 */
public class TestObject extends SuperClassObject {
    public TestObject() {
        this("default");
        System.out.println("test object constructor");
    }

    public TestObject(String tag) {
        this(true);
        System.out.println("test object constructor, tag is " + tag);
    }

    public TestObject(boolean isTrue) {
        super(isTrue);
        System.out.println("test object constructor, isTrue : " + isTrue);
    }
}
