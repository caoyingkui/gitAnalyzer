package cluster;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by kvirus on 2019/6/29 15:32
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class Method implements Serializable {
    public String id;
    public String gitPath;
    public String commitId;
    public String filePath;
    public String methodName;
    public String methodSignature;
    public int startLine;

    public String oldContent;
    public String newContent;

    public Method() {
        ;
    }

    public Method(JSONObject object) {
        id          = object.getString("id");
        commitId    = object.getString("commitId");
        gitPath     = object.getString("gitPath");

        filePath    = object.getString("filePath" );
        methodName  = object.getString("methodName");
        startLine   = object.getInt("startLine");
        //newContent  = object.getString("newContent");
        //oldContent  = object.getString("oldContent");
    }

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();

        object.put("id", id);
        object.put("commitId", commitId);
        object.put("gitPath", gitPath);
        object.put("filePath", filePath);
        object.put("methodName", methodName);
        object.put("startLine", startLine);
        object.put("newContent", newContent);
        object.put("oldContent", oldContent);

        return object;
    }
}
