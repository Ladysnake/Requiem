package ladysnake.dissolution.core;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ASMUtil {
    public static char[] parseMethodArguments(String desc) {
        String[] splitDesc = splitMethodDesc(desc);
        char[] returnChars = new char[splitDesc.length];
        int count = 0;
        for(String type : splitDesc) {
            if(type.startsWith("L") || type.startsWith("[")) {
                returnChars[count] = 'L';
            }
            else {
                if(type.length() > 1) { throw new RuntimeException(); }
                returnChars[count] = type.charAt(0);
            }
            count += 1;
        }
        return returnChars;
    }

    public static String[] splitMethodDesc(String desc) {
        int beginIndex = desc.indexOf('(');
        int endIndex = desc.lastIndexOf(')');
        if((beginIndex == -1 && endIndex != -1) || (beginIndex != -1 && endIndex == -1)) {
            System.err.println(beginIndex);
            System.err.println(endIndex);
            throw new RuntimeException();
        }
        String x0;
        if(beginIndex == -1 && endIndex == -1) {
            x0 = desc;
        }
        else {
            x0 = desc.substring(beginIndex + 1, endIndex);
        }
        Pattern pattern = Pattern.compile("\\[*L[^;]+;|\\[[ZBCSIFDJ]|[ZBCSIFDJ]"); //Regex for desc \[*L[^;]+;|\[[ZBCSIFDJ]|[ZBCSIFDJ]
        Matcher matcher = pattern.matcher(x0);
        ArrayList<String> listMatches = new ArrayList<>();
        while(matcher.find()) {
            listMatches.add(matcher.group());
        }
        return listMatches.toArray(new String[0]);
    }

    static int getReturnCode(String desc) {
        char t = desc.charAt(desc.indexOf(')')+1);
        int code;
        switch (t) {
            case 'Z':
            case 'B':
            case 'C':
            case 'S':
            case 'I':
                code = Opcodes.IRETURN;
                break;
            case 'J':
                code = Opcodes.LRETURN;
                break;
            case 'F':
                code = Opcodes.FRETURN;
                break;
            case 'D':
                code = Opcodes.DRETURN;
                break;
            case 'V':
                code = Opcodes.RETURN;
                break;
            default:
                code = Opcodes.ARETURN;
        }
        return code;
    }

    static class MethodKey {
        String name, desc;

        MethodKey(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodKey methodKey = (MethodKey) o;
            return Objects.equals(name, methodKey.name) &&
                    Objects.equals(desc, methodKey.desc);
        }

        @Override
        public int hashCode() {

            return Objects.hash(name, desc);
        }
    }

    /**
     * Immutable class storing a method's information
     */
    static class MethodInfo {
        final int retCode;
        final int access;
        final String signature;
        final String[] exceptions;
        final List<ParameterNode> params;
        final boolean abstr;

        MethodInfo(MethodNode methodNode) {
            this.abstr = (methodNode.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT;
            this.retCode = abstr ? Opcodes.RETURN : methodNode.instructions.getLast().getOpcode();
            this.access = methodNode.access;
            this.signature = methodNode.signature;
            this.exceptions = methodNode.exceptions == null ? null : methodNode.exceptions.toArray(new String[0]);
            // clone method parameters
            this.params = methodNode.parameters == null ? null : methodNode.parameters.stream().map(p -> new ParameterNode(p.name, p.access)).collect(Collectors.toList());
        }

    }
}
