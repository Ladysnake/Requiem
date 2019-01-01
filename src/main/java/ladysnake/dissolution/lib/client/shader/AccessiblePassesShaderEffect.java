package ladysnake.dissolution.lib.client.shader;

import net.minecraft.client.gl.PostProcessShader;
import org.apiguardian.api.API;

import java.util.List;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(status = INTERNAL)
public interface AccessiblePassesShaderEffect {
    List<PostProcessShader> getPasses();
}
