#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

// 0.0 = No effect, 1.0 = Max pixelation (based on u_amount)
uniform float u_progress;

// The resolution of the grid at max pixelation (e.g., 16.0, 16.0)
uniform vec2 u_amount;

void main() {
    // 1. Calculate the size of a single square
    vec2 size = 1.0 / u_amount;

    // 2. Calculate the "snapped" coordinate (the center of the square)
    vec2 snappedUV = (floor(v_texCoords / size) + 0.5) * size;

    // 3. Interpolate between original UV and snapped UV based on progress
    // mix(a, b, t) is (1-t)*a + t*b
    vec2 p = mix(v_texCoords, snappedUV, u_progress);

    gl_FragColor = texture2D(u_texture, p) * v_color;
}