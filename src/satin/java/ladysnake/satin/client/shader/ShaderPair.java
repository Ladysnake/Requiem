package ladysnake.satin.client.shader;

import net.minecraft.util.Identifier;

public class ShaderPair {
    private final Identifier fragment;
    private final Identifier vertex;

    ShaderPair(Identifier fragment, Identifier vertex) {
        this.fragment = fragment;
        this.vertex = vertex;
    }

    public Identifier getFragment() {
        return fragment;
    }

    public Identifier getVertex() {
        return vertex;
    }
}
