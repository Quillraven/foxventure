#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float u_desaturation;

void main() {
    vec4 color = texture2D(u_texture, v_texCoords);
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    color.rgb = mix(color.rgb, vec3(gray), u_desaturation);
    gl_FragColor = color * v_color;
}