## RxMvi
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE.md)
[![Language](https://img.shields.io/badge/language%3A-Kotlin-blue)](https://kotlinlang.org)
<!-- [![Version]()]() -->

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
implementation 'com.maximcode:RxMvi:x.x.x'
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

    fun validateText(uiEvent: Observable<CharSequence>) {
        disposingActions += store.dispatch(uiEvent) { MainAction.ValidateText(it.toString()) }
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

    fun loadPosts() {
        val loaded = store.currentState.loaded
        if(!loaded) {
            disposingActions += store.dispatch { Actions.Load }
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


#### Demo Apps

You can find all demo apps over [here](demo).

### Contributing

If you like this project, or are using it in your app, consider starring the repository to show your support. 
Contributions from the community are very welcome.
