package ladysnake.satin.client.shader;

import net.minecraft.util.Identifier;

import java.util.Map;

public class ShaderRegistryEvent {
    private final Map<Identifier, ShaderPair> registeredShaders;

    public ShaderRegistryEvent(Map<Identifier, ShaderPair> registeredShaders) {
        this.registeredShaders = registeredShaders;
    }

    /**
     * Convenience method to register a fragment shader using the default <tt>vertex_base</tt>. <br>
     * The corresponding program will be created and linked during the next ResourceManager reloading.
     *
     * <p>
     *     <u>Example:</u> Using the identifier <tt>gaspunk:gas_overlay</tt> will register a shader using
     *     the file <tt>assets/ladylib/shaders/vertex_base.vsh</tt> as its vertex shader and
     *     <tt>assets/gaspunk/shaders/gas_overlay.fsh</tt> as its fragment shader.
     * </p>
     *
     * @param identifier the name of the fragment shader, minus the file extension
     */
    public void registerFragmentShader(Identifier identifier) {
        registerShader(
                identifier,
                BaseShaders.BASE_VERTEX,
                new Identifier(identifier.getNamespace(), ShaderHelper.SHADER_LOCATION_PREFIX + identifier.getPath() + ".fsh")
        );
    }

    /**
     * Convenience method to register a vertex shader using the default <tt>fragment_base</tt>. <br>
     * The corresponding program will be created and linked during the next ResourceManager reloading.
     *
     * <p>
     *     <u>Example:</u> Using the identifier <tt>gaspunk:gas_overlay</tt> will register a shader using
     *     the file <tt>assets/gaspunk/shaders/gas_overlay.vsh</tt> as its vertex shader and
     *     <tt>assets/ladylib/shaders/fragment_base.fsh</tt> as its fragment shader.
     * </p>
     *
     * @param identifier the name of the fragment shader, minus the file extension
     */
    public void registerVertexShader(Identifier identifier) {
        registerShader(
                identifier,
                new Identifier(identifier.getNamespace(), ShaderHelper.SHADER_LOCATION_PREFIX + identifier.getPath() + ".vsh"),
                BaseShaders.BASE_FRAGMENT
        );
    }

    /**
     * Convenience method to register a shader with the fragment and vertex shaders having the same name <br>
     * The corresponding program will be created and linked during the next ResourceManager reloading.
     *
     * <p>
     *     <u>Example:</u> Using the identifier <tt>gaspunk:gas_overlay</tt> will register a shader using
     *     the file <tt>assets/gaspunk/shaders/gas_overlay.vsh</tt> as its vertex shader and
     *     <tt>assets/gaspunk/shaders/gas_overlay.fsh</tt> as its fragment shader.
     * </p>
     *
     * @param identifier the common name or relative location of both shaders, minus the file extension
     */
    public void registerShader(Identifier identifier) {
        registerShader(
                identifier,
                new Identifier(identifier.getNamespace(), ShaderHelper.SHADER_LOCATION_PREFIX + identifier.getPath() + ".vsh"),
                new Identifier(identifier.getNamespace(), ShaderHelper.SHADER_LOCATION_PREFIX + identifier.getPath() + ".fsh")
        );
    }

    /**
     * Registers a shader using the given vertex and fragment. <br>
     * The corresponding program will be created and linked during the next ResourceManager reloading
     *
     * @param identifier a unique resource location that will be used to load this shader
     * @param vertex     the file name of the vertex shader, extension included
     * @param fragment   the file name of the fragment shader, extension included
     */
    public void registerShader(Identifier identifier, Identifier vertex, Identifier fragment) {
        registeredShaders.put(identifier, new ShaderPair(fragment, vertex));
    }

}