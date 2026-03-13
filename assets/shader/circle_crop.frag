#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float u_progress; /* 0.0 (closed) to 1.0 (fully open) */
uniform float u_ratio; /* screen width / screen height */
uniform vec4 u_bgcolor; /* color outside the circle */
uniform vec2 u_center; /* circle center */

void main()
{
    // 1. Offset the coordinates by the custom center
    // 2. Scale the X-axis by the ratio to keep the circle round
    vec2 diff = v_texCoords - u_center;
    diff.x *= u_ratio;

    float dist = length(diff);

    // Calculate the distance to the furthest possible corner to ensure
    // the circle can cover the whole screen regardless of u_center.
    float maxDist = max(
        max(length(vec2(0.0, 0.0) - u_center), length(vec2(1.0, 0.0) - u_center)),
        max(length(vec2(0.0, 1.0) - u_center), length(vec2(1.0, 1.0) - u_center))
    ) * u_ratio;

    float radius = u_progress * maxDist;

    vec4 pixelColor = texture2D(u_texture, v_texCoords) * v_color;

    // Smoothstep provides a cleaner, anti-aliased edge than step()
    float threshold = smoothstep(radius - 0.01, radius + 0.01, dist);

    gl_FragColor = mix(pixelColor, u_bgcolor, threshold);
}