package git.fileDiff.file;

import git.description.Description;
import git.fileDiff.Diff;
import git.fileDiff.field.ChangedField;
import git.fileDiff.field.DelField;
import git.fileDiff.field.FieldDiff;
import git.fileDiff.field.NewField;
import git.fileDiff.method.ChangedMethod;
import git.fileDiff.method.DelMethod;
import git.fileDiff.method.MethodDiff;
import git.fileDiff.method.NewMethod;
import git.fileDiff.type.DiffType;
import git.git.ClassParser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by kvirus on 2019/4/20 19:22
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class DelClass extends FileDiff {
    public String name = "";

    public String path = "";

    public String content = "";

    public ClassParser parser;

    String parent = "";

    List<String> interfaces = new ArrayList<>();

    public HashSet<DelField> delFields = new HashSet<>();

    public HashSet<DelMethod> delMethods = new HashSet<>();

    public DelClass(String commitId, Diff diff, String content, String path, ClassParser parser) {
        this.commitId = commitId;
        this.diff = diff;
        this.content = content;
        this.path = path;

        this.parser = parser;
        name = parser.name;
        parent = parser.parent;
        interfaces.addAll(parser.interfaces);
    }

    @Override
    public HashSet<String> getChangedFiledNames() {
        if (changedFiledNames == null) {
            changedFiledNames = new HashSet<>();
            for (DelField field : delFields)
                changedFiledNames.add(field.name);
        }
        return changedFiledNames;
    }

    @Override
    public HashSet<String> getChangedMethodNames() {
        if (changedMethodNames == null) {
            changedMethodNames = new HashSet<>();

            for (DelMethod method: delMethods)
                changedMethodNames.add(method.name);
        }

        return changedMethodNames;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public List<FieldDiff> getFields() {
        List<FieldDiff> result = new ArrayList<>();
        result.addAll(delFields);
        return result;
    }

    @Override
    public List<MethodDiff> getMethods() {
        List<MethodDiff> result = new ArrayList<>();
        result.addAll(delMethods);
        return result;
    }

    @Override
    public List<FieldDiff> getFields(String fieldName, boolean withComment) {
        ArrayList<FieldDiff> result = new ArrayList<>();
        for (DelField field: delFields) {
            if (field.name.equals(fieldName) && (!withComment || field.comment.length() > 0))
                result.add(field);
        }
        return result;
    }

    @Override
    public List<MethodDiff> getMethods(String methodName, boolean withComment) {
        ArrayList<MethodDiff> result = new ArrayList<>();
        for (DelMethod method: delMethods) {
            if (method.name.equals(methodName) && (!withComment || method.comment.length() > 0))
                result.add(method);
        }
        return result;
    }

    @Override
    public void fieldDiff() {
        parser.getFields().forEach(f -> {
            DelField field = new DelField(f.name, f.fullName, f.comment);
            delFields.add(field);
        });
    }

    @Override
    public void methodDiff() {
        parser.getMethods().forEach(m -> {
            DelMethod method = new DelMethod(m, this, this.commitId);
            method.type = DiffType.Class_Delete;
            delMethods.add(method);
        });
    }

    @Override
    public void parse() {
        fieldDiff();
        methodDiff();
    }

    @Override
    protected void printFields() {
        System.out.println(path);
        System.out.println("    Deleted:");
        for (DelField f: delFields) {
            System.out.println("        " + f.name);
        }
    }

    @Override
    protected void printMethods() {
        System.out.println("delete methods");
        for (DelMethod method: delMethods)
            System.out.println("\t" + method.fullName);

    }

    @Override
    public String toString() {
        return name + " : Class";
    }
}
