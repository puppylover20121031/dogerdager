package com.unpuppyable.dogerdager;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

public final class PostProcessor implements Disposable {

    private final SpriteBatch batch = new SpriteBatch();
    private final ShaderProgram shader;
    private FrameBuffer fbo;
    private float time;
    private boolean glitch;

    public PostProcessor() {
        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(VERT, FRAG);
        if (!shader.isCompiled()) {
            throw new IllegalStateException("post shader failed: " + shader.getLog());
        }
    }

    public void resize(int width, int height) {
        if (fbo != null) fbo.dispose();
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
    }

    public void setGlitch(boolean glitch) {
        this.glitch = glitch;
    }

    public void toggleGlitch() {
        glitch = !glitch;
    }

    public void capture() {
        if (fbo != null) fbo.begin();
    }

    public void render(float delta) {
        if (fbo == null) return;
        fbo.end();
        time += delta;

        var region = new TextureRegion(fbo.getColorBufferTexture());
        region.flip(false, true);

        batch.getProjectionMatrix().setToOrtho2D(0, 0, fbo.getWidth(), fbo.getHeight());
        batch.setShader(shader);
        batch.begin();
        shader.setUniformf("u_time", time);
        shader.setUniformf("u_resolution", fbo.getWidth(), fbo.getHeight());
        shader.setUniformf("u_glitch", glitch ? 1f : 0f);
        batch.draw(region, 0, 0, fbo.getWidth(), fbo.getHeight());
        batch.end();
        batch.setShader(null);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shader.dispose();
        if (fbo != null) fbo.dispose();
    }

    private static final String VERT = """
            attribute vec4 a_position;
            attribute vec4 a_color;
            attribute vec2 a_texCoord0;
            uniform mat4 u_projTrans;
            varying vec4 v_color;
            varying vec2 v_texCoords;
            void main() {
                v_color = a_color;
                v_texCoords = a_texCoord0;
                gl_Position = u_projTrans * a_position;
            }
            """;

    private static final String FRAG = """
            #ifdef GL_ES
            precision mediump float;
            #endif
            varying vec4 v_color;
            varying vec2 v_texCoords;
            uniform sampler2D u_texture;
            uniform float u_time;
            uniform vec2 u_resolution;
            uniform float u_glitch;

            float rand(vec2 c) { return fract(sin(dot(c, vec2(12.9898, 78.233))) * 43758.5453); }

            void main() {
                vec2 uv = v_texCoords;
                if (u_glitch > 0.5) {
                    float line = rand(vec2(floor(uv.y * 90.0), floor(u_time * 11.0)));
                    if (line > 0.93) uv.x += (line - 0.93) * 0.35;
                }
                vec2 px = 1.0 / u_resolution;
                vec3 base = texture2D(u_texture, uv).rgb;
                vec3 bloom = vec3(0.0);
                for (int i = 0; i < 8; i++) {
                    float a = float(i) * 0.7853982;
                    vec2 dir = vec2(cos(a), sin(a));
                    bloom += max(texture2D(u_texture, uv + dir * px * 4.0).rgb - 0.45, 0.0);
                    bloom += max(texture2D(u_texture, uv + dir * px * 8.0).rgb - 0.45, 0.0) * 0.6;
                }
                bloom *= 0.12;
                vec3 col = base + bloom * 1.5;
                if (u_glitch > 0.5) {
                    float ca = 0.0014 + 0.004 * step(0.97, rand(vec2(floor(u_time * 9.0), 3.0)));
                    col.r += texture2D(u_texture, vec2(uv.x + ca, uv.y)).r - base.r;
                    col.b += texture2D(u_texture, vec2(uv.x - ca, uv.y)).b - base.b;
                    col *= 0.92 + 0.08 * sin(uv.y * u_resolution.y * 3.14159);
                }
                gl_FragColor = vec4(col, 1.0) * v_color;
            }
            """;
}
