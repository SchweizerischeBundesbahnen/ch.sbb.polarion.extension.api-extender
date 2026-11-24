# CLAUDE.md

## Gotchas

- **`ch.sbb.polarion.extension.generic`** is the parent project providing reusable infrastructure for all Polarion plugins in this org (settings framework, REST base classes, OSGi helpers, etc.). Before implementing anything cross-cutting, check if it already exists there.
- **Maven Settings**: Builds require `.mvn/settings.xml` (JFrog Artifactory, Sonatype credentials via env vars). CI passes it with `-s .mvn/settings.xml`.
- **Polarion Dependencies**: You must extract dependencies from the Polarion installer using [polarion-artifacts-deployer](https://github.com/SchweizerischeBundesbahnen/polarion-artifacts-deployer) before the Maven build will work.
- **Local Polarion Installation**: Requires `POLARION_HOME` environment variable. Use the `local-install-into-polarion` Maven profile: `mvn clean install -P local-install-into-polarion`
- **After any code change**: Delete `<POLARION_HOME>/data/workspace/.config` before restarting Polarion or changes won't be picked up.
- **Remote Debugging**: Add to Polarion's `config.sh`: `JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"`
- **Logging**: Polarion logs: `<POLARION_HOME>/polarion/logs/main/*.log`
- **Branch conventions**: Conventional commits enforced by commitizen (pre-commit hook). Feature branches: `feature/<name>`, bug fixes: `fix/<name>`, LTS branches: `release-v*` (e.g., `release-v6`).
- **Pre-commit hooks block internal patterns**: some org-specific identifiers are treated as secrets. Run `pre-commit run -a` after implementation.
- **The documentation is a site, and its articles are listed in three places.** `QUICK_START.md`, `USER_GUIDE.md`, `REST_API.md`, `VELOCITY_TOOLS.md` and `VELOCITY_2.md` are rendered by markdown2html in `generate-sources` into `webapp/api-extender-app/html/<id>.html` and shown by the app as one documentation site (see [`ui/README.md`](ui/README.md#the-documentation-site)), the way pdf-exporter and excel-importer do. An article needs an entry in `ui/src/docs/docs.config.json`, its markdown file and its own execution in the pom - `npm run build` fails when a manifest article was not rendered. The single `documentation` node of `hivemodule.xml` opens `quick-start`. Cross-links between articles are plain relative `.md` links, so they work on GitHub too. `README.md` is the About page and only links to the articles.
