# REST API

Scripts read and write [project custom fields](USER_GUIDE.md#project-custom-fields) and
[global records](USER_GUIDE.md#global-records) through the REST API, and search text with regular expressions. The
`REST API` page under `API Extender` is a Swagger UI with all endpoints, where they can be tried out. The OpenAPI
specification is also in the repository: [docs/openapi.json](docs/openapi.json).

| What           | Value                                                                          |
|----------------|--------------------------------------------------------------------------------|
| Base URL       | `https://<polarion-host>/polarion/api-extender/rest/api`                       |
| Authentication | A Polarion personal access token in the header `Authorization: Bearer <token>` |
| Values         | JSON: `{"value": "<value>"}`                                                   |
| Errors         | A JSON body with the reason in `message`                                       |

A script on a Polarion page uses the path `/internal` instead of `/api`, with the session of the user and without a
token. See [Writing values from a Live Report Page](USER_GUIDE.md#writing-values-from-a-live-report-page).

The examples below use `curl` with these two variables:

```bash
API=https://polarion.example.com/polarion/api-extender/rest/api
TOKEN=<personal access token>
```

## Project custom fields

| Request                                      | Purpose                         |
|----------------------------------------------|---------------------------------|
| `GET /projects/{projectId}/keys/{key}`       | Read the value of a key.        |
| `POST /projects/{projectId}/keys/{key}`      | Write the value of a key.       |
| `DELETE /projects/{projectId}/keys/{key}`    | Remove a key and its value.     |

Write a value:

```bash
curl -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
     -d '{"value": "jdoe"}' \
     "$API/projects/elibrary/keys/release_manager"
```

Read it:

```bash
curl -H "Authorization: Bearer $TOKEN" "$API/projects/elibrary/keys/release_manager"
```

```json
{"value": "jdoe"}
```

Remove it:

```bash
curl -X DELETE -H "Authorization: Bearer $TOKEN" "$API/projects/elibrary/keys/release_manager"
```

## Global records

| Request                 | Purpose                         |
|-------------------------|---------------------------------|
| `GET /records/{key}`    | Read the value of a key.        |
| `POST /records/{key}`   | Write the value of a key.       |
| `DELETE /records/{key}` | Remove a key and its value.     |

The requests are the same as for project custom fields, without the project:

```bash
curl -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
     -d '{"value": "support@example.com"}' \
     "$API/records/support_contact"
```

## Responses

| Response | Meaning                                                                                                     |
|----------|-------------------------------------------------------------------------------------------------------------|
| `200`    | `GET`: the value is in the body.                                                                            |
| `204`    | `POST` and `DELETE`: the value was written or removed, also when the key had no value.                      |
| `400`    | `POST` without a body (`value is not provided`), or `POST` and `DELETE` of a standard field of the project. |
| `401`    | No token, or a token which is not valid.                                                                    |
| `403`    | The user has none of the [roles which may write](USER_GUIDE.md#who-may-write).                              |
| `404`    | `GET` of a key without a value, or a project which does not exist.                                          |

`POST` with `{"value": null}` or `{}` removes the key, the same as `DELETE`.
`POST` without the header `Content-Type: application/json` fails with `500`.

## Regex tool

`POST /regex-tool/find-matches` finds all matches of a regular expression in a text, the same as
[`$regexTool`](VELOCITY_TOOLS.md#regex-tool). It takes a `multipart/form-data` request with the parts `text` and
`regex`:

```bash
curl -H "Authorization: Bearer $TOKEN" \
     -F "text=Dates: 23-10-2025 and 01-12-2024" -F 'regex=(\d{2})-(\d{2})-(\d{4})' \
     "$API/regex-tool/find-matches"
```

```json
{"0": ["23-10-2025", "23", "10"], "1": ["01-12-2024", "01", "12"]}
```

The keys are the numbers of the matches. For the content of the arrays see
[Regex tool](VELOCITY_TOOLS.md#regex-tool). An invalid regular expression is answered with `400`, a missing part
with `500`.

## Common endpoints

The extension also has the endpoints which all extensions of this family share, for example:

| Request                                                   | Purpose                                                                               |
|-----------------------------------------------------------|---------------------------------------------------------------------------------------|
| `GET /version`                                            | The version of the extension.                                                         |
| `GET /settings/{feature}/names/Default/content?scope=...` | The roles which may write: feature `project_custom_fields` or `global_records`.       |
| `PUT /settings/{feature}/names/Default/content?scope=...` | Saves the roles, the same as the [administration pages](USER_GUIDE.md#who-may-write). |
| `GET /roles?scope=...`                                    | The global and project roles which can be selected.                                   |

The scope is empty for the repository and `project/<projectId>/` for a project. The Swagger UI describes all of them.
