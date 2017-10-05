package hello.bean;

/**
 * Created by Lotus on 9/5/2017.
 */
public class OClass {
    public int id;
    public String className;

    public OClass() {
    }

    public OClass(String className) {
        this.className = className;
    }

    public OClass(int id, String className) {
        this.id = id;
        this.className = className;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
