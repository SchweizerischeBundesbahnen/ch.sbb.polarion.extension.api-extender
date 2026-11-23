[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender&metric=bugs)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender&metric=coverage)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=SchweizerischeBundesbahnen_ch.sbb.polarion.extension.api-extender)

# API extension for Polarion ALM

This Polarion extension provides additional functionality which is not implemented in standard Polarion API for some reason:

- Custom fields for projects and global records for the whole repository, readable and writable from Velocity scripts and the REST API.
- Helper tools and Apache Velocity Tools 3 for Velocity scripts.
- Velocity Engine 2.4.1 for scripts of Live Report Pages, LiveDoc documents and Classic Wiki Pages (experimental).

> [!IMPORTANT]
> Starting from version 2.0.0 only latest version of Polarion is supported.
> Right now it is Polarion 2606.

## Quick start

The latest version of the extension can be downloaded from the [releases page](../../releases/latest) and installed to Polarion instance without necessity to be compiled from the sources.
The extension should be copied to `<polarion_home>/polarion/extensions/ch.sbb.polarion.extension.api-extender/eclipse/plugins` and changes will take effect after Polarion restart.
> [!IMPORTANT]
> Don't forget to clear `<polarion_home>/data/workspace/.config` folder after extension installation/update to make it work properly.

## Documentation

| Page | Contents |
| --- | --- |
| [Quick start](QUICK_START.md) | From the download to a working extension |
| [User guide](USER_GUIDE.md) | Project custom fields, global records and who may write them |
| [REST API](REST_API.md) | Reading and writing the values from a script |
| [Velocity tools](VELOCITY_TOOLS.md) | `$collectionsTool`, `$regexTool` and Apache Velocity Tools 3 as `$velocityToolsV3` |
| [Velocity 2](VELOCITY_2.md) | Velocity Engine 2.4.1 in widgets, LiveDoc documents and Classic Wiki Pages |

The same pages are available in Polarion: Administration ➙ API Extender ➙ Documentation.

For developers:

- [Development Guide](./DEVELOPMENT.md) - Comprehensive guide for setting up development environment and contributing to this project
- [Contributing Guidelines](./CONTRIBUTING.md) - Guidelines for contributing to this project
- [Coding Standards](./CODING_STANDARDS.md) - Coding standards and best practices
- [Release Process](./RELEASE.md) - Information about the release process

## REST API

This extension provides REST API. OpenAPI Specification can be obtained [here](docs/openapi.json).
