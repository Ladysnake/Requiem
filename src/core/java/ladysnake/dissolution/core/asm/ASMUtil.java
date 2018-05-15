package ladysnake.dissolution.core.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ASMUtil {

    /**
     * Quick debug method to print an instruction list
     *
     * @param instructions the instructions to print
     */
    public static void dump(InsnList instructions) {
        Iterator<AbstractInsnNode> iterator = instructions.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode node = iterator.next();
            String s;
            if (node instanceof LabelNode)
                s = "LABEL";
            else if (node instanceof LineNumberNode)
                s = "LINE";
            else if (node.getOpcode() >= 0)
                s = Printer.OPCODES[node.getOpcode()];
            else s = node.toString();
            s += " ";
            if (node instanceof VarInsnNode) s += ((VarInsnNode) node).var;
            else if (node instanceof MethodInsnNode) s += ((MethodInsnNode) node).name + ((MethodInsnNode) node).desc;
            else if (node instanceof JumpInsnNode) s += ((JumpInsnNode) node).label.getLabel().toString();
            else if (node instanceof LabelNode) s += ((LabelNode) node).getLabel().toString();
            else if (node instanceof LineNumberNode) s += ((LineNumberNode) node).line;
            System.out.println(s);
        }
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

        @Override
        public String toString() {
            return name + " " + desc;
        }
    }

    static class GetterSetterPair {
        String getter, setter;

        GetterSetterPair(String getter, String setter) {
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodKey methodKey = (MethodKey) o;
            return Objects.equals(getter, methodKey.name) &&
                    Objects.equals(setter, methodKey.desc);
        }

        @Override
        public int hashCode() {
            return Objects.hash(getter, setter);
        }

        @Override
        public String toString() {
            return "get:" + getter + "; set:" + setter;
        }
    }

    /**
     * Immutable class storing a method's information
     */
    static class MethodInfo {
        final int access;
        final String signature;
        final String[] exceptions;
        final List<ParameterNode> params;
        final boolean abstr;

        MethodInfo(MethodNode methodNode) {
            this.abstr = (methodNode.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT;
            this.access = methodNode.access;
            this.signature = methodNode.signature;
            this.exceptions = methodNode.exceptions == null ? null : methodNode.exceptions.toArray(new String[0]);
            // clone method parameters
            this.params = methodNode.parameters == null ? null : methodNode.parameters.stream().map(p -> new ParameterNode(p.name, p.access)).collect(Collectors.toList());
        }

        public MethodInfo(int access, String signature, String[] exceptions, List<ParameterNode> params, boolean abstr) {
            this.access = access;
            this.signature = signature;
            this.exceptions = exceptions;
            this.params = params;
            this.abstr = abstr;
        }

        @Override
        public String toString() {
            return "MethodInfo{" +
                    "access=" + access +
                    ", signature='" + signature + '\'' +
                    ", exceptions=" + Arrays.toString(exceptions) +
                    ", params=" + params +
                    ", abstr=" + abstr +
                    '}';
        }
    }
}
