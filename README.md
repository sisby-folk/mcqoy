<!--suppress HtmlDeprecatedTag, XmlDeprecatedElement -->
<center>
<h1>QCONF OVER YACL (MCQOY)</h1>
</center>

<center><img alt="mod preview" src="https://cdn.modrinth.com/data/tNmWwdI2/images/b58da00b068e403fbf5a7335d750798fe7969e90.png"/></center>

<center>
<i>He's dead, Jim.</i><br/>
Requires <a href="https://modrinth.com/mod/yacl">YACL</a>.
</center>

---

**McQoy** is a simple mod that automatically generates YACL configuration screens for mods using Kaleido.

### IT SEEMS I'VE MISSED YOU

[Some mods](https://modrinth.com/collection/zZVgWFum) use [Kaleido](https://github.com/sisby-folk/kaleido-config) to provide a `.toml` configuration - like our own [Surveyor](https://modrinth.com/mod/surveyor), or [Crunchy Crunchy](https://modrinth.com/mod/crunchy-crunchy-advancements), or [PicoHUD](https://modrinth.com/mod/picohud).

The files look nice, but you can't edit them in-game. With McQoy, you can! Just drop the mod in and you're set.

### I DON'T NEED A DOCTOR, I AM A DOCTOR

To set this up for your own mod, check out [Kaleido#usage](https://github.com/sisby-folk/kaleido-config#usage) - any loader and version, doesn't matter, it's universal.<br/>
When setting the config up, just make sure to specify your folder _or_ filename as your mod ID, so McQoy can find it.

<center><img alt="qconfig example" src="https://cdn.modrinth.com/data/tNmWwdI2/images/2b36d05c06cc8c9dcf11b2b22d66c607c2d80e6e.png"/></center>
<center><i>You may find this arrangement terribly pleasant.</i></center><br/>

Then, on any McQoy-compatible version, installing McQoy will add screens! No setup, and no hard dependency.<br/>
For player visibility, you can also set McQoy as an `optional` dependency on Modrinth. (Or `required`, I'm not a cop.)

## IF I'M NOT CAREFUL, I'LL END UP TALKING TO MYSELF

Oh, right - a little background and credit is due:

### ALL I GOT LEFT IS MY BONES

[Quilt Config](https://github.com/QuiltMC/quilt-config) isn't built on quilt, or even minecraft - It's a _pure-java_ configuration library.

That means it works on every loader, and every version of minecraft, with no changes required.

The API is even designed for mods like McQoy to utilize - with fancy annotation-based metadata. Thanks qconf!

### I'M NOT A MAGICIAN, SPOCK

Kaleido is just a wrapper for Quilt Config that shadows the library to avoid path conflicts with Quilt Loader.

With quilt seemingly being sunset, we'll be sure to follow the maintainers if they decide to fork it (hi ix!)

## FASCINATING

All mods are built on the work of many others.

This project is based on [Quilt Config](https://github.com/QuiltMC/quilt-config) - and exists only because it's a genuinely great pure-java config library. 

**Special thanks to [ix0rai](https://modrinth.com/user/ix0rai) and [cassiancc](https://github.com/cassiancc)** for providing helpful code snippets and having good ideas to begin with.


