# cortex
scala micro framework

## Quick Look

Here is a very simple implementation of a Cortex Application:

```scala
object Main extends App {
	object app extends Cortex {
		def port = 8080
		def controllers = Seq(
			new Controller {
				register("/helloWorld", req => {
					Option("Hello, world!")
				}, ContentType.TextHtml, HttpMethod.GET)
		})
		def views = Seq()
	}

	app.start()
}
```

Running `curl http://localhost:8080/helloWorld` will produce the text "Hello, world!". Simple as that.
