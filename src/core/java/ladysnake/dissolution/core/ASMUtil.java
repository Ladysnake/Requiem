package ladysnake.dissolution.core;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ASMUtil {

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
