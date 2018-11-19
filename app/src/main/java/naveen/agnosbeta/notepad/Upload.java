package naveen.agnosbeta.notepad;

/**
 * Created by naVeen on 18-02-2018.
 */

class Upload {

    public String name;
    public String url;
    public String desc;
    public String size;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public Upload() {
    }

    public Upload(String name, String url,String desc,String size) {
        this.name = name;
        this.url = url;
        this.desc= desc;
        this.size= size;
    }

    public String getName() {
        return name;
    }

    public String getUrl() { return url; }

    public String getDesc() {
        return desc;
    }

    public String getSize() {
        return size;
    }
}