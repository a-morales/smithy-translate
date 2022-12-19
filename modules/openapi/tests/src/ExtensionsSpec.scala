/* Copyright 2022 Disney Streaming
 *
 * Licensed under the Tomorrow Open Source Technology License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://disneystreaming.github.io/TOST-1.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package smithytranslate.openapi

final class ExtensionsSpec extends munit.FunSuite {

  test("extensions: accurate conversion") {
    val openapiString = """|openapi: '3.0.'
                     |info:
                     |  title: test
                     |  version: '1.0'
                     |paths: {}
                     |components:
                     |  schemas:
                     |    MyString:
                     |      type: string
                     |      x-float: 1.0
                     |      x-string: foo
                     |      x-int: 1
                     |      x-array: [1, 2, 3]
                     |      x-null: null
                     |      x-boolean: true
                     |      x-not-boolean: false
                     |      x-obj:
                     |        a: 1
                     |        b: 2
                     |""".stripMargin

    val expectedString = """|namespace foo
                      |
                      |use alloy.openapi#openapiExtensions
                      |
                      |@openapiExtensions(
                      | "x-float": 1.0,
                      | "x-array": [1, 2, 3],
                      | "x-not-boolean": false,
                      | "x-string": "foo",
                      | "x-int": 1,
                      | "x-null": null,
                      | "x-obj": {
                      |   a: 1,
                      |   b: 2
                      | },
                      | "x-boolean": true
                      |)
                      |string MyString
                      |""".stripMargin

    TestUtils.runConversionTest(openapiString, expectedString)
  }

  test("extensions: captured from wherever") {
    val openapiString = """|openapi: '3.0.'
                     |info:
                     |  title: test
                     |  version: '1.0'
                     |  x-service: foo
                     |paths:
                     |  /test:
                     |    post:
                     |      operationId: testOperationId
                     |      x-op: foo
                     |      requestBody:
                     |        x-request-body: foo
                     |        required: true
                     |        content:
                     |          application/json:
                     |            schema:
                     |              type: object
                     |              x-object: foo
                     |              properties:
                     |                s:
                     |                  type: string
                     |              required:
                     |                - s
                     |      responses:
                     |        '200':
                     |          x-response: foo
                     |          content:
                     |            application/json:
                     |              schema:
                     |                type: object
                     |                properties:
                     |                  sNum:
                     |                    type: integer
                     |""".stripMargin

    val expectedString = """|namespace foo
                      |
                      |use alloy.openapi#openapiExtensions
                      |use smithytranslate#contentType
                      |
                      |@openapiExtensions("x-service": "foo")
                      |service FooService {
                      |    operations: [
                      |        TestOperationId
                      |    ]
                      |}
                      |
                      |@http(
                      |    method: "POST",
                      |    uri: "/test",
                      |    code: 200,
                      |)
                      |@openapiExtensions("x-op": "foo")
                      |operation TestOperationId {
                      |  input: TestOperationIdInput,
                      |  output: TestOperationId200
                      |}
                      |
                      |@openapiExtensions("x-response": "foo")
                      |structure TestOperationId200 {
                      |    @httpPayload
                      |    @required
                      |    @contentType("application/json")
                      |    body: TestOperationId200Body,
                      |}
                      |
                      |structure TestOperationId200Body {
                      |    sNum: Integer,
                      |}
                      |
                      |structure TestOperationIdInput {
                      |    @httpPayload
                      |    @required
                      |    @openapiExtensions("x-request-body": "foo")
                      |    @contentType("application/json")
                      |    body: TestOperationIdInputBody,
                      |}
                      |
                      |@openapiExtensions("x-object": "foo")
                      |structure TestOperationIdInputBody {
                      |  @required
                      |  s: String
                      |}
                      |""".stripMargin

    TestUtils.runConversionTest(openapiString, expectedString)
  }

}
