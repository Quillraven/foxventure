#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float u_progress;
uniform vec2 u_amount;
uniform float u_desaturation;

void main() {
    vec2 size = 1.0 / u_amount;
    vec2 snappedUV = (floor(v_texCoords / size) + 0.5) * size;
    vec2 p = mix(v_texCoords, snappedUV, u_progress);

    vec4 color = texture2D(u_texture, p);

    // Desaturation
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    color.rgb = mix(color.rgb, vec3(gray), u_desaturation);

    gl_FragColor = color * v_color;
}