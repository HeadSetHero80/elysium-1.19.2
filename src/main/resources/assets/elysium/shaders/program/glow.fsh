#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D MainSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

uniform vec3 Phosphor;

out vec4 fragColor;

void main() {
    vec4 CurrTexel = texture(DiffuseSampler, texCoord);
    vec4 BgTexel = texture(MainSampler, texCoord);

    fragColor = vec4(mix(BgTexel.rgb + CurrTexel.rgb*0.8, CurrTexel.rgb, length(CurrTexel.rgb)/3), 1.0);
}
