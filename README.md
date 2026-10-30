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

This Polarion extension provides additional functionality which is not implemented in standard Polarion API for some reason.

> [!IMPORTANT]
> Starting from version 2.0.0 only latest version of Polarion is supported.
> Right now it is Polarion 2606.

## Custom field for project

Polarion project does not support setting custom fields out of the box.
This API extension can be used to solve this problem.

This API can be called using REST API and in Velocity Context.

## Quick start

The latest version of the extension can be downloaded from the [releases page](../../releases/latest) and installed to Polarion instance without necessity to be compiled from the sources.
The extension should be copied to `<polarion_home>/polarion/extensions/ch.sbb.polarion.extension.api-extender/eclipse/plugins` and changes will take effect after Polarion restart.
> [!IMPORTANT]
> Don't forget to clear `<polarion_home>/data/workspace/.config` folder after extension installation/update to make it work properly.

## Documentation

- [Development Guide](./DEVELOPMENT.md) - Comprehensive guide for setting up development environment and contributing to this project
- [Contributing Guidelines](./CONTRIBUTING.md) - Guidelines for contributing to this project
- [Coding Standards](./CODING_STANDARDS.md) - Coding standards and best practices
- [Release Process](./RELEASE.md) - Information about the release process

## Polarion configuration

### REST API

This extension provides REST API. OpenAPI Specification can be obtained [here](docs/openapi.json).

### Live Report Page

Get version:

```velocity
#set ($version = $customFieldsProject.getVersion())
#if ($version)
    API Extender version = $version
#end
```

or

```velocity
#set ($version = $globalRecords.getVersion())
#if ($version)
    API Extender version = $version
#end
```

Get custom field value:

```velocity
#set ($field = $customFieldsProject.getCustomField('elibrary', 'custom_field'))
#if ($field)
    $field.getValue()
    <br>
#end
```

Get global record value:

```velocity
#set ($field = $globalRecords.getRecord('record_name'))
#if ($field)
    $field.getValue()
    <br>
#end
```

Due to Polarion limitations we are not able to save custom fields in Live Report Page using Velocity, but we can use JavaScript for this.

Set custom field value:

```html
<input id='cfp_project' type='text' value='elibrary' name='project'/>
<input id='cfp_key' type='text' value='custom_field' name='key'/>
<input id='cfp_value' type='text' value='custom_value' name='value'/>
<script>
    function save_project_custom_field() {
        const project = document.getElementById('cfp_project').value;
        const key = document.getElementById('cfp_key').value;
        const value = document.getElementById('cfp_value').value;
        const requestBody = (value === null || value === "") ? "" : JSON.stringify({'value': value});

        fetch('/polarion/api-extender/rest/internal/projects/' + project + '/keys/' + key, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: requestBody
        })
                .then(response => {
                    if (response.ok) {
                        return "Saved!"
                    } else {
                        return response.text()
                    }
                })
                .then(text => {
                    alert(text)
                });
    }
</script>
<button onclick='save_project_custom_field()'>Save</button>
```

Set global record value:

```html
<input id='record_key' type='text' value='record_name' name='record_name'/>
<input id='record_value' type='text' value='record_value' name='record_value'/>
<script>
    function save_record() {
        const key = document.getElementById('record_key').value;
        const value = document.getElementById('record_value').value;
        const requestBody = (value === null || value === "") ? "" : JSON.stringify({'value': value});

        fetch('/polarion/api-extender/rest/internal/records/' + key, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: requestBody
        }).then(response => {
            if (response.ok) {
                return "Saved!"
            } else {
                return response.text()
            }
        }).then(text => {
            alert(text)
        });
    }
</script>
<button onclick='save_record()'>Save</button>
```

Note that internal API in URL should be used.

### Classic Wiki Page

Get custom field value:

```velocity
#set($projects = $projectService.searchProjects("","id"))

#foreach($project in $projects)
    #set($projectId = $project.id)
    #set($field = $customFieldsProject.getCustomField($projectId, 'custom_field'))
    #if ($field)
        $projectId custom_field = $field.getValue()
        <br>
        #set($field = false)
    #end
#end
```

Get global record value:

```velocity
$globalRecords.getRecord('record_name')
```

Set custom field value:

```velocity
$customFieldsProject.setCustomField('elibrary', 'custom_field', 'new_value')
```

Set global record value:

```velocity
$globalRecords.setRecord('record_name', 'record_value')
```

## Tools
This extension includes several velocity tools to facilitate development and maintenance.

### Collections Tool

Sort a map by its values in ascending or descending order:
```velocity
#set($sortedMap = $collectionsTool.sortMap($unsortedMap, true))
```

Wrap given list of objects into a IPObjectList:
```velocity
#set(list = $collectionsTool.convertArrayListToWeakPObjectList($workItems))
```

### Regex Tool

Extract regex matches in the given text, returning the matches and their respective groups:
```velocity
#set($matches = $regexTool.findMatches('(\d+)', "test123and456"))
```

## Velocity Tools 3

Polarion renders Velocity scripts with Velocity Engine 1.4 and includes [Apache Velocity Tools 2.0](https://velocity.apache.org/tools/2.0/generic.html).
This extension adds a selection of the generic tools of [Velocity Tools 3.1](https://velocity.apache.org/tools/3.1/tools-summary.html) as the `$velocityToolsV3` object.

`$velocityToolsV3` is available in:

- Live Report Pages (Script blocks and widgets)
- LiveDoc documents ("Wiki Content" blocks)
- Classic Wiki Pages

The built-in Polarion tools (`$date`, `$math`, `$number`, `$esc` and others) do not change.
Both versions can be used in the same script:

```velocity
Tools 2.0: $math.mul(2, '1.5')
Tools 3.1: $velocityToolsV3.math.mul(2, '1.5')
```

> [!NOTE]
> The built-in Tools 2.0 objects exist only in Live Report Pages.
> In LiveDoc documents and Classic Wiki Pages, `$velocityToolsV3` is the only way to use these tools.

Get a tool by its name, for example `$velocityToolsV3.date` or `$velocityToolsV3.get('date')`.
List the available tool names:

```velocity
$velocityToolsV3.names
```

> [!IMPORTANT]
> Velocity Engine 1.4 does not support decimal number literals such as `1234.5`.
> Give decimal numbers as strings: `$velocityToolsV3.number.format('#,##0.00', '1234.5')`.
>
> Velocity Engine 1.4 cannot call methods with a variable number of arguments (varargs).
> These methods stay as text in the page, for example `display.message`, `display.printf`, or `math.add` with more than two numbers.
>
> `#foreach` does not go through the `json` and `xml` results directly. Add `.iterator()`: `#foreach($item in $xml.children().iterator())`.

The [Javadoc of Velocity Tools 3.1](https://velocity.apache.org/tools/3.1/apidocs/org/apache/velocity/tools/generic/package-summary.html) describes all methods of each tool.

### collection

[CollectionTool](https://velocity.apache.org/tools/3.1/apidocs/org/apache/velocity/tools/generic/CollectionTool.html) sorts collections, arrays and maps, and splits strings into arrays.

```velocity
#foreach($item in $velocityToolsV3.collection.sort($workItems, 'id:desc'))
    $item.id
#end
#foreach($part in $velocityToolsV3.collection.split('a,b,c'))($part)#end
```

Compared to Tools 2.0: CollectionTool is new in Tools 3.
It replaces SortTool (`$sorter` in Live Report Pages), which is deprecated in Tools 3.
`sort` removes `null` elements. SortTool 2.0 returns `null` for a collection with `null` elements.

### date

[ComparisonDateTool](https://velocity.apache.org/tools/3.1/apidocs/org/apache/velocity/tools/generic/ComparisonDateTool.html) formats and parses dates, and compares two dates.

```velocity
$velocityToolsV3.date.format('yyyy-MM-dd HH:mm', $velocityToolsV3.date.date)
#set($d = $velocityToolsV3.date.toDate('iso', '2026-10-28T13:05:00'))
$velocityToolsV3.date.format('iso_tz', $d)
Created $velocityToolsV3.date.whenIs($workItem.created)
```

Compared to Tools 2.0:

- New standard formats: `iso`, `iso_tz` (ISO 8601) and `intl`, `intl_tz` (international format).
- New `toDate` and `toCalendar` methods with a locale and a time zone.
- `whenIs`, `difference` and `timespan` give relative dates such as `32 minutes ago` or `1 month`.
  The built-in `$date` is a plain DateTool and does not have these methods.

### display

[DisplayTool](https://velocity.apache.org/tools/3.1/apidocs/org/apache/velocity/tools/generic/DisplayTool.html) prepares values for display: truncate text, join lists, give default values, select singular or plural, remove HTML tags.

```velocity
$velocityToolsV3.display.truncate('A long text to cut here', 10)
$velocityToolsV3.display.list($names, ', ', ' and ')
$velocityToolsV3.display.alt($value, 'n/a')
$velocityToolsV3.display.plural($count, 'item')
$velocityToolsV3.display.stripTags($html)
```

Compared to Tools 2.0: Polarion does not include DisplayTool in the Velocity context.

### esc

[EscapeTool](https://velocity.apache.org/tools/3.1/apidocs/org/apache/velocity/tools/generic/EscapeTool.html) escapes text for HTML, XML, JavaScript, Java, SQL, URLs and property files.

```velocity
$velocityToolsV3.esc.html('<b>&</b>')
$velocityToolsV3.esc.url('a b&c')
$velocityToolsV3.esc.unurl('a%20b%26c')
```

Compared to Tools 2.0:

- New `unurl` method decodes URL-encoded text.
- `xml` escapes only the XML special characters. Tools 2.0 also changes non-ASCII letters to numeric entities (`Zürich` becomes `Z&#252;rich`).

In LiveDoc documents and Classic Wiki Pages, the built-in `$esc` is a different Polarion object with fewer methods.

### json

[JsonTool](https://velocity.apache.org/tools/3.1/apidocs/org/apache/velocity/tools/generic/JsonTool.html) parses a JSON string. Get values by key or by index, or go through an array with `#foreach`.

```velocity
#set($json = $velocityToolsV3.json.parse('{"owner": "jdoe", "limits": {"max": 5}}'))
Owner: $json.owner, max: $json.limits.max
#set($list = $velocityToolsV3.json.parse('["a", "b"]'))
#foreach($value in $list.iterator())($value)#end
```

Compared to Tools 2.0: JsonTool is new in Tools 3.

> [!NOTE]
> Only `parse` is available. `fetch`, `read` and the other methods that load JSON from a URL or a resource return `null`.
> This prevents scripts from sending requests from the Polarion server.

### link

[LinkTool](https://velocity.apache.org/tools/3.1/apidocs/org/apache/velocity/tools/generic/LinkTool.html) builds and changes URLs. It encodes query parameters, and `encode` and `decode` work on any text.
Methods such as `uri`, `path` and `param` return a new link. Each `$velocityToolsV3.link` call gives a new LinkTool.
Parameters are separated with `&amp;`, which is correct in HTML. Call `setXHTML(false)` on your own copy to get `&`.

```velocity
$velocityToolsV3.link.uri('https://jira.example.com/issues/').param('jql', 'project = ABC AND status = Open')
$velocityToolsV3.link.uri($url).params
$velocityToolsV3.link.encode('type:requirement AND status:open')
```

Compared to Tools 2.0: Polarion does not include LinkTool in the Velocity context.

> [!NOTE]
> LinkTool does not know the URL of the Polarion server. Give the server address with `uri(...)` to get absolute links.
> Polarion keeps its page address after `#`, for example `/polarion/#/project/elibrary/workitems?query=...`.
> `param` adds parameters before `#`. For a Polarion search link, build the part after `#` yourself and encode the query with `encode`.

### log

[LogTool](https://velocity.apache.org/tools/3.1/apidocs/org/apache/velocity/tools/generic/LogTool.html) writes a message to the Polarion log: `error`, `warn`, `info`, `debug`, `trace`.
The call adds nothing to the page.

```velocity
$velocityToolsV3.log.info("Report started for project $projectId")
#if(!$workItem)
    $velocityToolsV3.log.warn('Work item not found')
#end
```

The messages use the logger `ch.sbb.polarion.extension.api_extender.velocity.tools_v3.PolarionLogTool`.
The log level configuration of Polarion decides if `debug` and `trace` messages appear.

Compared to Tools 2.0: LogTool is new in Tools 3.

### loop

[LoopTool](https://velocity.apache.org/tools/3.1/apidocs/org/apache/velocity/tools/generic/LoopTool.html) gives information about the current `#foreach` iteration: first and last item, index, count. It can also skip items or stop the loop.

Each `$velocityToolsV3.loop` call gives a new LoopTool. Keep one instance in a variable:

```velocity
#set($loop = $velocityToolsV3.loop)
#foreach($item in $loop.watch($workItems))
    $item.id#if(!$loop.last), #end
#end
```

Compared to Tools 2.0: Polarion does not include LoopTool in the Velocity context.
Velocity Engine 1.4 has only `$velocityCount`. It has no `$foreach.first`, `$foreach.last` or `$foreach.hasNext`.

### math

[MathTool](https://velocity.apache.org/tools/3.1/apidocs/org/apache/velocity/tools/generic/MathTool.html) does arithmetic on numbers and numeric strings: `add`, `sub`, `mul`, `div`, `idiv`, `mod`, `pow`, `abs`, `round`, `roundTo`, `ceil`, `floor`, `min`, `max`, `random`.

```velocity
$velocityToolsV3.math.add(2, 3)
$velocityToolsV3.math.roundTo(2, '3.14159')
$velocityToolsV3.math.bitwiseAnd(6, 3)
```

Compared to Tools 2.0:

- The result type follows the arguments: `mul(2, '1.5')` gives `3.0`. Tools 2.0 gives `3`.
- New bitwise operations: `bitwiseAnd`, `bitwiseOr`, `bitwiseXOr`.
- `toInteger`, `toDouble` and `toNumber` are deprecated. Use the `number` tool to parse numbers.

### number

[NumberTool](https://velocity.apache.org/tools/3.1/apidocs/org/apache/velocity/tools/generic/NumberTool.html) formats and parses numbers: `currency`, `integer`, `percent`, `number` or a custom pattern.

```velocity
$velocityToolsV3.number.format('#,##0.00', '1234.5')
$velocityToolsV3.number.percent('0.25')
$velocityToolsV3.number.toNumber('42')
```

Compared to Tools 2.0: the methods are the same.
Tools 3 moves the number parsing and formatting of the deprecated ConversionTool into NumberTool.

### render

`render` evaluates a template string with Velocity Engine 2.4.1, which is part of this extension.
It is a replacement for the [RenderTool](https://velocity.apache.org/tools/3.1/apidocs/org/apache/velocity/tools/generic/RenderTool.html) of Velocity Tools 3.
The RenderTool needs a Velocity 2 context, and Polarion scripts cannot give one.

Give the variables of the template as up to five key-value pairs, or as one `Map`:

```velocity
$velocityToolsV3.render.eval('Hello $name', 'name', $user.name)
$velocityToolsV3.render.eval('#set($x = 1.5)$x')
$velocityToolsV3.render.eval('#foreach($i in $items)$i#if($foreach.hasNext), #end#end', 'items', $workItems)
```

The template can use the Velocity 2 syntax, for example decimal literals and `$foreach.hasNext`.

> [!IMPORTANT]
> Limitations:
> - The template does not see the variables of the page. It sees only the variables given in the call.
> - Write the template in single quotes. Velocity Engine 1.4 replaces variables in double-quoted strings before `render` gets the template.
> - `#parse` and `#include` are not available. The engine cannot read files or class path resources.
> - Reflection is blocked, for example `$s.class.forName(...)` and `$s.class.classLoader`.
> - More than five key-value pairs are not possible. Use a `Map` instead.
> - A template error gives `null`, and the page shows the call as text. The Polarion log contains the error.

Compared to Tools 2.0: Polarion does not include RenderTool in the Velocity context.

### xml

[XmlTool](https://velocity.apache.org/tools/3.1/apidocs/org/apache/velocity/tools/generic/XmlTool.html) parses an XML string. Find nodes with XPath, read attributes and text, and go through child nodes with `#foreach`.

```velocity
#set($xml = $velocityToolsV3.xml.parse('<items><item id="1">first</item><item id="2">second</item></items>'))
Second: $xml.find('/items/item[@id="2"]').text
#foreach($item in $xml.children().iterator())
    $item.attr('id'): $item.text
#end
```

Compared to Tools 2.0: Polarion does not include XmlTool in the Velocity context.

> [!NOTE]
> Only `parse` is available. `fetch` and `read`, which load XML from a URL or a resource, return `null`.
> The parser does not resolve external entities and DTDs, so a document cannot read server files.

### Not included tools

Some generic tools of Velocity Tools 3 are not available:

- `class`, `field`: they give scripts access to any Java class.
- `context`: it needs the Velocity context of the page. Live Report Pages give scripts no reference to it.
- `import`: it loads content from URLs and resources. This is a security risk, the same as for `json`.
- `text`: it reads resource bundles from the class path of this extension.
- View tools (`params`, `cookie`, `pager` and others): they need a servlet request.

The ListTool (`$lists`) and IteratorTool (`$mill`) of Live Report Pages do not exist in Velocity Tools 3.
