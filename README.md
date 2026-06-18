# Applied Mekanistics

Use Mekanism resources through your ME network.

## Requirements

Cleanroom is required for the necessity.

[Cleanroom](https://github.com/CleanroomMC/Cleanroom) is a 1.12.2 Forge fork, providing newer toolchain, new APIs and 99% compatibility.

Need Java25.

The following mods are required for this mod:
* Applied Energistics 2 supergiant
* Mekanism

This is now currently based off AE2 supergiant, it depends on Cleanroom so that the regular and the uel is not allowed with this mod.

## Upstream

Please note that the modid in this repository has been changed to `mekeng`

This project is based on [AppliedEnergistics/Applied-Mekanistics](https://github.com/AppliedEnergistics/Applied-Mekanistics).

The upstream repository remains the original Appmek project and the primary reference for design, behavior, licensing, and attribution.

## Maven

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    var mod_version = "v1.1.0"
    implementation "com.github.FormlessDragon:Applied-Mekanistics:${mod_version}:dev"
}
```

## License

Everything is under the [LGPLv3][lgpl], but assets are under [CC BY-NC-SA 3.0][cc]

[lgpl]: https://spdx.org/licenses/LGPL-3.0-or-later.html
[cc]: https://creativecommons.org/licenses/by-nc-sa/3.0/

## Credits

Thanks to Team Applied Energistics, AlgorithmX2, the upstream AE2 contributors, and everyone involved in the original project:
[AppliedEnergistics/Applied-Energistics-2](https://github.com/AppliedEnergistics/Applied-Energistics-2).

Thanks to Team Applied Energistics and everyone involved in the original project:
[AppliedEnergistics/Applied-Mekanistics](https://github.com/AppliedEnergistics/Applied-Mekanistics)