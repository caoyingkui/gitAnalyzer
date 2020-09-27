package git.git;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kvirus on 2019/6/30 14:59
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class DeclarationVisitor extends ASTVisitor {

    String sourceCode = "";

    List<Method> methods    = new ArrayList<>();
    List<Field>  fields     = new ArrayList<>();

    ClassParser parser      = null;

    public DeclarationVisitor(ClassParser parser) {
        this.parser = parser;
        sourceCode = parser.sourceCode;
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node) {
        return false;
    }

    @Override
    public boolean visit(EnumConstantDeclaration node) {
        String qualifiedName = getDeclarationPath(node);
        String name = node.getName().toString();
        String fullName = qualifiedName.length() > 0 ? qualifiedName + "." : "";
        fullName += name;

        int startLine = parser.getLine(node.getStartPosition());

        Field field = new Field("ENUM", fullName, name, "", startLine);
        fields.add(field);

        return false;
    }

    @Override
    public boolean visit(FieldDeclaration node) {
        String qualifiedName = getDeclarationPath(node);
        int startLine = parser.getLine(node.getStartPosition());
        String filedType = node.getType().toString();
        for (Object f : node.fragments()) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) f;
            String name = fragment.getName().toString();
            String fullName = qualifiedName.length() > 0 ? qualifiedName  + "." : "";
            fullName += name;
            Field field = new Field(filedType,
                    fullName,
                    name,
                    "",
                    startLine);
            fields.add(field);
        }
        return false;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        String methodName       = node.getName().toString();
        String name             = node.getName().toString();
        StringBuilder parStr           = new StringBuilder();
        List parameters         = node.parameters();
        String qualifiedName    = getDeclarationPath(node);
        if(parameters.size() > 0){
            for(Object par: parameters){
                if (par instanceof SingleVariableDeclaration) {
                    SingleVariableDeclaration p = (SingleVariableDeclaration) par;
                    String typeName = p.getType().toString();

                    // double a[][], double[][] a并不会执行
                    int dim = p.getExtraDimensions();
                    for (int i = 0; i < dim; i++) typeName += "[]";

                    // double ...
                    if (p.isVarargs()) typeName += " ...";

                    if (parStr.length() > 0)
                        parStr.append(",");
                    parStr.append(typeName);
                } else {
                    assert 1 == 2;
                }
            }
        }
        methodName += ("(" + parStr.toString().replaceAll(" ", "") + ")");

        int startPosition = node.getStartPosition();
        int endPosition = startPosition + node.getLength() - 1;
        String fullName = qualifiedName.length() > 0 ? qualifiedName + "." : "";
        fullName += methodName;

        try {
            MethodBuilder builder = new MethodBuilder();
            Method method = builder.setFullName(fullName).setName(name)
                    .setStartLine(parser.getLine(startPosition))
                    .setEndLine(parser.getLine(endPosition))
                    .setStartPos(startPosition)
                    .setEndPos(endPosition)
                    .setMethodContent(sourceCode.substring(startPosition, endPosition + 1))
                    .setComment("")
                    .setNode(node)
                    .build();
            methods.add(method);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public String getDeclarationPath(ASTNode node) {
        String path = "";
        node = node.getParent();
        while (node != null) {
            String name = "";

            if (node instanceof TypeDeclaration)        name = ((TypeDeclaration)(node)).getName().toString();
            else if (node instanceof EnumDeclaration)   name = ((EnumDeclaration)(node)).getName().toString();
            else if (node instanceof CompilationUnit) {
                PackageDeclaration pac = ((CompilationUnit)(node)).getPackage();
                if (pac != null) name = pac.getName().toString();
            }
            else System.out.println("declaration path error");

            if (name.length() > 0 ) {
                if (path.length() > 0) path = "." + path;
                path = name + path;
            }

            node = node.getParent();
        }

        return path;
    }
}
