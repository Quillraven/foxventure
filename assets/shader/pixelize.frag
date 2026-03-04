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
uniform float u_ratio; /* aspect ratio */
uniform vec2 u_squares_min; /* minimum number of squares (when the effect is at its higher level) */
uniform int u_steps; /* zero disable the stepping */

void main()
{
    float dist = u_steps>0 ? ceil(u_progress * float(u_steps)) / float(u_steps) : u_progress;
    vec2 squareSize = 2.0 * dist / u_squares_min;
    vec2 p = dist>0.0 ? (floor(v_texCoords / squareSize) + 0.5) * squareSize : v_texCoords;
    gl_FragColor = texture2D(u_texture, p) * v_color;
}