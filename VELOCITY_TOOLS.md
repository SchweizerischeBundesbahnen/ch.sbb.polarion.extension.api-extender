# Velocity tools

The extension adds these objects to the Velocity context of Live Report Pages, LiveDoc documents ("Wiki Content"
blocks) and Classic Wiki Pages:

| Object             | Purpose                                              |
|--------------------|------------------------------------------------------|
| `$collectionsTool` | Sorts maps and wraps lists of Polarion objects.      |
| `$regexTool`       | Finds the matches of a regular expression in a text. |
| `$velocityToolsV3` | The generic tools of Apache Velocity Tools 3.1.      |

`$customFieldsProject` and `$globalRecords`, which read and write project custom fields and global records, are
described in the [user guide](USER_GUIDE.md#values-in-velocity-scripts).

## Collections tool

`sortMap(map, ascending)` sorts a map by its values, which must be of one comparable type, for example all
integers. The result is a new map in the order of the values, ascending for `true` and descending for `false`.
Entries with the same value are all kept.

For example in a Classic Wiki Page:

```velocity
#set($counts = $objectFactory.newMap())
#set($void = $counts.put('E1', 1))
#set($void = $counts.put('E2', 0))
#set($void = $counts.put('E3', 2))
#set($void = $counts.put('E4', 0))
$collectionsTool.sortMap($counts, true)
```

The result is `{E2=0, E4=0, E1=1, E3=2}`; with `false` it is `{E3=2, E1=1, E2=0, E4=0}`.

`convertArrayListToWeakPObjectList(list)` wraps a list of Polarion objects, for example work items, into an
`IPObjectList`, which some Polarion methods require:

```velocity
#set($list = $collectionsTool.convertArrayListToWeakPObjectList($workItems))
```

## Regex tool

`findMatches(regex, text)` finds all matches of a regular expression in a text. The regular expression has the Java
syntax. The result is a map: the key is the number of the match, starting with `0`, and the value is an array of
strings.

```velocity
#set($matches = $regexTool.findMatches('(\d{2})-(\d{2})-(\d{4})', 'Dates: 23-10-2025 and 01-12-2024'))
#foreach($index in $matches.keySet())
    Match $index:#foreach($group in $matches.get($index)) [$group]#end<br>
#end
```

The array of a match has one element per capturing group of the regular expression. The first element is the whole
match, the next elements are the groups from the first on, and the last group is left out. In the example above,
the array of the first match is `23-10-2025`, `23`, `10`: the year is missing.
A regular expression without groups gives empty arrays, so add a group to get the matched text: `(\d+)` instead of
`\d+`.

The same search is available in the [REST API](REST_API.md#regex-tool).

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
- New variants of `toDate` and `toCalendar`: `toDate(value, locale[, timeZone])` and `toCalendar(format, value, locale, timeZone)`.
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
> - A `#macro` is available only inside the `eval` call that defines it.
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
