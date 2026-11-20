package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
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
    private static TextureRegion region;

    public PostProcessor() {
        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(VERT, FRAG);
        if (!shader.isCompiled()) {
            throw new IllegalStateException("post shader failed: " + shader.getLog());
        }
    }

    public void resize(int logicalWidth, int logicalHeight) {
        // resize() gets the window's logical size, but with desktop scaling the real pixel count
        // (back buffer) can be bigger. Capture at real pixels so the frame isn't drawn small and stretched.
        int width = Gdx.graphics.getBackBufferWidth();
        int height = Gdx.graphics.getBackBufferHeight();
        if (width <= 0 || height <= 0) {
            Gdx.app.error("PostProcessor", "Ignoring resize with invalid dimensions: " + width + "x" + height);
            return;
        }
        if (fbo != null) fbo.dispose();
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        region = new TextureRegion(fbo.getColorBufferTexture());
        region.flip(false, true);
    }

    public void setGlitch(boolean glitch) {
        this.glitch = glitch;
    }

    public void toggleGlitch() {
        glitch = !glitch;
    }

    public Boolean getGlitch() {
        return this.glitch;
    }

    public void capture() {
        if (fbo != null) fbo.begin();
    }

    public void render(float delta) {
        if (fbo == null) return;
        fbo.end();
        time += delta;
        // deleted creating TextureRegion from here because doing that 60 times/s when region doesn't change 60 times/s is suboptimal
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
                float burstClock = floor(u_time * 2.0);
                float burstChance = rand(vec2(burstClock, 9.0));
                float burstTime = fract(u_time * 2.0);
                float burst = step(0.82, burstChance) * (1.0 - smoothstep(0.10, 0.30, burstTime));
                if (u_glitch > 0.5) {
                    float line = rand(vec2(floor(uv.y * 75.0), floor(u_time * 8.0)));
                    if (line > 0.985) uv.x += (line - 0.985) * 0.12 * burst;

                    float tear = rand(vec2(floor(u_time * 3.0), 17.0));
                    if (tear > 0.965 && uv.y > 0.72 && uv.y < 0.78) uv.x += 0.018 * burst;
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
                    float ca = (0.0008 + 0.0018 * burst) * (0.5 + 0.5 * step(0.74, rand(vec2(floor(u_time * 6.0), 3.0))));
                    col.r += texture2D(u_texture, vec2(uv.x + ca, uv.y)).r - base.r;
                    col.b += texture2D(u_texture, vec2(uv.x - ca, uv.y)).b - base.b;

                    float scan = 0.94 + 0.06 * sin(uv.y * u_resolution.y * 3.14159);
                    col *= mix(1.0, scan, 0.35);

                    vec2 centered = uv * 2.0 - 1.0;
                    float vignette = 1.0 - smoothstep(0.35, 1.15, dot(centered, centered));
                    float dread = 0.18 + 0.10 * sin(u_time * 0.85);
                    col *= mix(0.62 - dread * burst, 1.0, vignette);
                    col.r += burst * 0.18 * (1.0 - vignette);

                    float grain = rand(vec2(gl_FragCoord.xy + floor(u_time * 24.0)));
                    col *= 0.96 + grain * 0.08 * burst;
                }
                gl_FragColor = vec4(col, 1.0) * v_color;
            }
            """;

}
