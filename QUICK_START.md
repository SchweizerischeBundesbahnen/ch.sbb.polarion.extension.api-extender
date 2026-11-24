# Quick start

The shortest way from the download to a working extension.

## Deploy API Extender to Polarion

The extension supports only the latest version of Polarion, currently Polarion 2606.

Take the file `ch.sbb.polarion.extension.api-extender-<version>.jar` from the page of
[releases](https://github.com/SchweizerischeBundesbahnen/ch.sbb.polarion.extension.api-extender/releases) and copy
it to the folder `<polarion_home>/polarion/extensions/ch.sbb.polarion.extension.api-extender/eclipse/plugins`.

## Restart Polarion

Stop Polarion.
Delete the `<polarion_home>/data/workspace/.config` folder.
Start Polarion.

The administration now has the node `API Extender`. Its `About` page shows the version of the extension. No
configuration is necessary to start.

## Ready to go

From here:

- The [user guide](USER_GUIDE.md) explains project custom fields and global records, who may write them and how
  to read and write them in Velocity scripts.
- The [REST API](REST_API.md) reads and writes the same values from a script.
- The [Velocity tools](VELOCITY_TOOLS.md) add helper tools and Apache Velocity Tools 3 to Velocity scripts.
- [Velocity 2](VELOCITY_2.md) renders scripts of Polarion pages with Velocity Engine 2.4.1.
