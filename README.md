<!--suppress HtmlDeprecatedTag, XmlDeprecatedElement -->
<center>
<h1>QCONF OVER YACL</h1>
</center>

<center><img alt="mod preview" src="https://upload.wikimedia.org/wikipedia/commons/9/90/DeForest_Kelley%2C_Dr._McCoy%2C_Star_Trek.jpg"/></center>

<center>
<i>He's dead, Jim.</i> 
</center>

---

**McQoy** A minimal compatibility layer that generates YACL configuration screens for QConfig mods.

### ALL I GOT LEFT IS MY BONES

The misleadingly-named [Quilt Config](https://github.com/QuiltMC/quilt-config) isn't built on quilt - it's not even built on minecraft. Quilt Config is a _java_ configuration library.

This means mods can JIJ it (or repackages like [Kaleido](https://github.com/sisby-folk/kaleido-config)) for a simple, clean-looking, file-based config that never breaks when minecraft updates.

### IF I'M NOT CAREFUL, I'LL END UP TALKING TO MYSELF

That's no fun though, is it? People want flashy, in-game-tweakable screens they can access through modmenu and forgelikes.

This mod seeks out all registered `ReflectiveConfig`-based configs, matches them to their Mod IDs, then provides them just that - no questions. 

### OFF THE DEEP END, MR. SCOTT

The thing is, QConf is stuffed to the nines with helpful annotations suitable for (or even designed for) a config GUI to read.

Despite being internally barebones, McQoy simply reads these values and passes them right on to YACL - you'd never guess it didn't require a real dependency mod dependency.

### I AM A DOCTOR, NOT AN ENGINEER

**The point of all this** is to cut down on the amount of mods that break between versions purely because of a bit of in-game config. Mods with just a few mixins often appreciate a small config, but they're forced into brittleness by larger libraries. YACL does a decent job of keeping API consistent between versions - McQoy takes it further and removes the version-specific API entirely. 

Modders often overestimate the complexity needed from their configuration. booleans, strings, maps of strings and booleans, few things are complicated enough to require actual custom GUI code. So why not call in a professional?

## FASCINATING

All mod projects are built on the work of many others.

Thanks to [ix0rai](https://modrinth.com/user/ix0rai) and [cassian](https://github.com/cassiancc/Item-Descriptions) for helping solidify the concept for this idea, which was floating around for far too long


