<img src="rxmvi_logo.png" alt="rxmvi-logo" width="261px" height="150px"/>
<br>

[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE.md)
[![Language](https://img.shields.io/badge/language%3A-Kotlin-blue)](https://kotlinlang.org)
[![Version](https://jitpack.io/v/merklol/RxMvi.svg)](https://jitpack.io/#merklol/RxMvi)


### What's this?

RxMvi is a simple Redux/MVI-like Android library that helps to manage state using RxJava 3.

### Installation

Add the Maven repository to your project root build.gradle

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency to module build.gradle

```groovy
implementation 'com.github.merklol:RxMvi:<version>'
```

### Usage

> Note: All examples are using [Hilt](https://dagger.dev/hilt/) for dependency injection,
> and the 'by viewModels()' Kotlin property delegate from [Android KTX](https://developer.android.com/kotlin/ktx).

### Example 1

A basic example how to use RxMvi in your project:

#### State

First of all, let's add a state class. 

```kotlin
data class MainState(val text: String = "")
```

#### Action

Then, let's add a sealed class to define the actions of our app.

> Note: All actions should implement the Action interface.

```kotlin
sealed class MainAction: Action {
    class ValidateText(val payload: String) : MainAction()
}
```

#### Reducer

Now, let's add our reducer by implementing the Reducer<**State**> interface.

```kotlin
class MainReducer: Reducer<MainState> {
    override fun reduce(state: MainState, action: Action): MainState {
        return when(action) {
            is MainAction.ValidateText -> state.copy(
                text = action.payload
            )
            else -> state
        }
    }
}
```

#### ViewModel

Then, we add a ViewModel. 

> Note: All ViewModels should extend RxMviViewModel<**State**> to get RxMvi functionality.

```kotlin
class MainViewModel @ViewModelInject constructor(
    private val store: Store<MainState>): RxMviViewModel<MainState>(store) {

    override val disposables = CompositeDisposable()

    fun validateText(uiEvent: Observable<CharSequence>) {
        disposables += store.dispatch(uiEvent) { MainAction.ValidateText(it.toString()) }
    }
}
```

#### View

Let's add our view(*Activity/Fragment/etc*).

> Note: All Views should extend RxMviView<**State, ViewModel: RxMviViewModel<State>**> to get RxMvi functionality.

```kotlin
@AndroidEntryPoint
class MainActivity: RxMviView<MainState, MainViewModel>() {
    override val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel.validateText(editTextView.textChanges())

    }

    override fun render(state: MainState) {
        if(state.text.length > 4) {
            editTextView.error = "The text is too long"
        }
    }
}
```

#### Store

Finally, let's provide a store to the app using Hilt.

```kotlin
@Module
@InstallIn(ApplicationComponent::class)
class MainModule {
    @Provides
    fun provideStore(): Store<MainState> {
        return createStore(MainReducer(), MainState(), middlewares(RxMviLogger()))
    }
}
```

#### Link 

A completed demo app [here](demo/demoapp4).

### Example 2

Using Middleware

#### State

Let's add a state class again. 

```kotlin
data class PostsState(
    val loading: Boolean = false,
    val loaded: Boolean = false,
    val error: Throwable? = null,
    val posts: List<Post> = listOf()
)
```

#### Action

Then, define the actions of the app.

```kotlin
sealed class Actions: Action {
    object Load: Actions()
}
```

#### Effect

Now, we need to define side effects.

```kotlin
sealed class Effects: Effect {
    object Loading: Effects()
    class Loaded(val payload: List<Post>): Effects()
    class Failed(val error: Throwable): Effects()
}
```

#### Middleware

After that, it's time to add a middleware.

> Middleware provides a way to interact with actions that have been dispatched to the store 
> before they reach the store's reducer.

```kotlin
class LoadingPosts(
    private val typicodeAPI: TypicodeAPI,
    private val mapper: Mapper<PostEntity, Post>): Middleware<PostsState> {

    override fun bind(state: Observable<PostsState>, actions: Observable<Action>): Observable<Action> {
        return actions.ofType(Actions.Load::class.java)
            .withLatestFrom(state) { action, currentState -> action to currentState }
            .flatMap {
                typicodeAPI.posts()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map<Effects> { result ->
                        Effects.Loaded(result.map { mapper.mapFromEntity(it) })
                    }
                    .onErrorReturn { Effects.Failed(it) }
                    .startWith(Observable.just(Effects.Loading))
            }
    }
}
```

#### Reducer

Next, let's add a reducer again.

```kotlin
class PostsReducer: Reducer<PostsState> {
    override fun reduce(state: PostsState, action: Action): PostsState {
        return when(action) {
            is Effects.Loaded -> state.copy(
                loaded = true,
                loading = false,
                posts = action.payload
            )

            is Effects.Failed -> state.copy(
                loaded = true,
                loading = false,
                error = action.error
            )

            is Effects.Loading -> state.copy(
                loading = true,
            )

            else -> state
        }
    }
}
```

#### ViewModel

Then, let's add a ViewModel. 

```kotlin
class PostsViewModel @ViewModelInject constructor(
    private val store: Store<PostsState>): RxMviViewModel<PostsState>(store) {

    override val disposables = CompositeDisposable()

    fun loadPosts() {
        val (_, loaded) = store.state.value
        if(!loaded) {
            disposables += store.dispatch { Actions.Load }
        }
    }
}
```

#### View

Then, add a view.

```kotlin
@AndroidEntryPoint
class PostsActivity : RxMviView<PostsState, PostsViewModel>() {
    override val viewModel: PostsViewModel by viewModels()

    private val adapter = RVAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)
        setRecyclerView()

        viewModel.loadPosts()
    }

    override fun render(state: PostsState) {
        when {
            state.loading -> progressView.visibility = View.VISIBLE
            state.loaded -> {
                if(state.error != null) {
                    errorView.text = state.error.message
                    errorView.visibility = View.VISIBLE
                }
                progressView.visibility = View.GONE
                adapter.addPosts(state.posts)
            }
        }
    }

    private fun setRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(RVMarginDecoration(this, 16, 16))
    }
}
```

#### Store

Lastly, let's provide a store to the app.

```kotlin
@Module
@InstallIn(ApplicationComponent::class)
class PostsModule {
    
    /*...*/

    @Provides
    fun providesStore(typicodeAPI: TypicodeAPI): Store<PostsState> {
        return createStore(
            PostsReducer(), PostsState(), middlewares(LoadingPosts(typicodeAPI, PostMapper()))
        )
    }
}
```

#### Link 

A completed demo app [here](demo/demoapp2).


### Logging

You can enable logging by passing an instance of [RxMviLogger](rxmvi/src/main/java/com/maximcode/rxmvi/logger/RxMviLogger.kt) to the store at the initialization stage.

```kootlin
 createStore(Reducer(), State(), middlewares(RxMviLogger()))
```

#### Logs Example:

```console
I/rxMvi-logger: ️action type = Calculating; current state = { CounterState(isCalculating=true, isHintDisplayed=false, result=0) }
I/rxMvi-logger: ️action type = Increment; current state = { CounterState(isCalculating=true, isHintDisplayed=false, result=0) }
```

### Testing

Here is are a few examples of how to test the business logic in your app.

**1. Unit tests**

Let's first mock a middleware

```kotlin
@Before
    private fun mockMiddleware() {
            val action = MainEffect.IncrementSuccess(1)
            every { inc.bind(any(), any()) } answers { Observable.just(action) }
        }
```

Now let's test state changes out using the TestObserver from RxJava's testing APIs

```kotlin
@Test
    fun `when increment counter triggered, should hide hint and change result to 1`() {
        viewModel.incrementCounter(Observable.just(Unit))
        val state = testObserver.values()[0] as CounterState
        assertThat(state.isHintDisplayed, `is`(false))
        assertThat(state.result, `is`(1))
    }
```

To test the middleware, let's subscribe to the actions and check whether we receive the right effects

```kotlin
@Before
    fun setup() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        val inc = IncrementMiddleware()
        inc.bind(state, action).subscribe(testObserver)
    }

@Test
    fun `after delay, should return IncrementSuccess`() {
        testObserver.awaitDone(4, TimeUnit.SECONDS)
        val effect = testObserver.values()[1] as MainEffect.IncrementSuccess
        assertThat(effect.payload, `is`(1))
    }
```

**2. UI Tests**

For UI tests, we are going to use the Espresso Framework. First, let's add a test rule that is going to be responsible for taking screenshots during the tests.

```kotlin
class ScreenshotTestRule : TestWatcher() {

    @Throws(IOException::class)
    override fun finished(description: Description?) {
        super.finished(description)

        val className = description?.testClass?.simpleName ?: "NullClassname"
        val methodName = description?.methodName ?: "NullMethodName"
        val filename = "$className - $methodName"

        val capture = Screenshot.capture()
        capture.name = filename
        capture.format = Bitmap.CompressFormat.PNG

        val processors = HashSet<androidx.test.runner.screenshot.ScreenCaptureProcessor>()
        processors.add(ScreenCaptureProcessor())
        capture.process(processors)
    }
}
```

Next, we add and set up a UI test

```kotlin
@RunWith(AndroidJUnit4::class)
class CounterActivityTest {
    @get:Rule
    var activityRule: ActivityScenarioRule<CounterActivity>
            = ActivityScenarioRule(CounterActivity::class.java)

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(activityRule).around(ScreenshotTestRule())

    /*...*/
}
```

Now, we can add test cases using the Espresso Framework

```kotlin
    @Test
    fun when_dec_button_clicked_should_display_minus1() {
        onView(withId(R.id.decBtnView)).perform(click())
        onView(withId(R.id.counterView)).check(matches(withText("-1")))
    }

    @Test
    fun when_inc_button_clicked_should_display_progressBar() {
        onView(withId(R.id.incBtnView)).perform(click())
        onView(withId(R.id.progressView)).check(matches(isDisplayed()))
    }

    @Test
    fun when_showHint_button_clicked_should_hide_hintView() {
        onView(withId(R.id.showHintBtnView)).perform(click())
        onView(withId(R.id.hintView)).check(matches(not(isDisplayed())))
    }
```

#### Demo Apps

You can find all demo apps over [here](demo).

### Contributing

If you like this project, or are using it in your app, consider starring the repository to show your support. 
Contributions from the community are very welcome.
