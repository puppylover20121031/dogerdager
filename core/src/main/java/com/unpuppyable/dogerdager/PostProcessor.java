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
    private boolean enabled = true;

    public PostProcessor() {
        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(VERT, GLITCH);
        if (!shader.isCompiled()) {
            throw new IllegalStateException("glitch shader failed: " + shader.getLog());
        }
    }

    public void resize(int width, int height) {
        if (fbo != null) fbo.dispose();
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
    }

    public void toggle() {
        enabled = !enabled;
    }

    public void capture() {
        if (enabled && fbo != null) fbo.begin();
    }

    public void render(float delta) {
        if (!enabled || fbo == null) return;
        fbo.end();
        time += delta;

        var region = new TextureRegion(fbo.getColorBufferTexture());
        region.flip(false, true);

        batch.getProjectionMatrix().setToOrtho2D(0, 0, fbo.getWidth(), fbo.getHeight());
        batch.setShader(shader);
        batch.begin();
        shader.setUniformf("u_time", time);
        shader.setUniformf("u_resolution", fbo.getWidth(), fbo.getHeight());
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

    private static final String GLITCH = """
            #ifdef GL_ES
            precision mediump float;
            #endif
            varying vec4 v_color;
            varying vec2 v_texCoords;
            uniform sampler2D u_texture;
            uniform float u_time;
            uniform vec2 u_resolution;

            float rand(vec2 c) { return fract(sin(dot(c, vec2(12.9898, 78.233))) * 43758.5453); }

            void main() {
                vec2 uv = v_texCoords;
                float line = rand(vec2(floor(uv.y * 90.0), floor(u_time * 11.0)));
                if (line > 0.93) uv.x += (line - 0.93) * 0.35;
                float ca = 0.0014 + 0.004 * step(0.97, rand(vec2(floor(u_time * 9.0), 3.0)));
                float r = texture2D(u_texture, vec2(uv.x + ca, uv.y)).r;
                float g = texture2D(u_texture, uv).g;
                float b = texture2D(u_texture, vec2(uv.x - ca, uv.y)).b;
                vec3 col = vec3(r, g, b);
                col *= 0.92 + 0.08 * sin(uv.y * u_resolution.y * 3.14159);
                gl_FragColor = vec4(col, 1.0) * v_color;
            }
            """;
}
