#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float u_progress;
uniform vec2 u_amount;

void main() {
    vec2 size = 1.0 / u_amount;
    vec2 snappedUV = (floor(v_texCoords / size) + 0.5) * size;
    vec2 p = mix(v_texCoords, snappedUV, u_progress);

    gl_FragColor = texture2D(u_texture, p) * v_color;
}