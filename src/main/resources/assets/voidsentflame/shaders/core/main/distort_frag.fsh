#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0; // texture
uniform sampler2D Sampler2; // normal map or ripple map

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

//void main() {
//    // Basic ripple distortion via normal map scrolling
//    vec2 scrollUV = texCoord0 * u_uvScale + vec2(GameTime * u_scrollSpeed, GameTime * u_scrollSpeed);
//    vec3 norm = texture(Sampler0, scrollUV).rgb * 2.0 - 1.0;
//    // distort UV for ripple effect
//    vec2 rippleUV = texCoord0 + norm.xy * u_rippleStrength;
//
//    // sample a noise or ripple texture for highlight
//    float rippleMask = texture(Sampler2, rippleUV * 2.0).r;
//
//    // compute highlight factor
//    float highlight = smoothstep(0.5, 1.0, rippleMask);
//
//    // mix base water and ripple color
//    vec3 color = mix(u_baseColor, u_rippleColor, highlight);
//
//    // maybe add specular/glint for golden shimmer
//    float spec = pow(highlight, 8.0);
//    color += spec * u_rippleColor * 0.4;
//
//    fragColor = vec4(color, 1.0);
//}

void main() {
    vec4 color = texture2D(Sampler0, texCoord0) * ColorModulator;

    fragColor = color; 
    //apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}