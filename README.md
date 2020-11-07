## RxMvi
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE.md)
[![Version]()]()
[![Language](https://img.shields.io/badge/language%3A-Kotlin-blue)](https://kotlinlang.org)

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

##### Example 1
A basic example how to use RxMvi

##### State
```kotlin
data class MyState(val message: String)
```

##### Action
```kotlin
sealed class MyAction: Action {
    class ShowMessage(val payload: String) : MyAction()
}
```

##### ViewModel
```kotlin
class MyViewModel @ViewModelInject constructor(
    val store: Store<MyState>): RxMviViewModel<MyState>(store) {

    fun showMessage(uiEvent: Observable<Unit>) {
        disposingActions.add(store.dispatch(uiEvent){ ShowMessage("Hello world!") })
    }
}
```

##### View
```kotlin
@AndroidEntryPoint
class MyActivity : RxMviView<MyState>() {
    override val viewModel: MyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_counter)
    
            viewModel.showMessage(btnView.clicks())   
        }
    
        override fun render(state: MyState) {
            myTextView.text = state.message
        }
    }
```

##### Example 2
Using Middleware

##### State
```kotlin
data class MyState(val message: String)
```

##### Action
```kotlin
sealed class MyAction: Action {
    class ShowMessage(val payload: String) : MyAction()
}
```

##### ViewModel
```kotlin
class MyViewModel @ViewModelInject constructor(
    val store: Store<MyState>): RxMviViewModel<MyState>(store) {

    fun showMessage(uiEvent: Observable<Unit>) {
        disposingActions.add(store.dispatch(uiEvent){ ShowMessage("Hello world!") })
    }
}
```

##### View
```kotlin
@AndroidEntryPoint
class MyActivity : RxMviView<MyState>() {
    override val viewModel: MyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_counter)
    
            viewModel.showMessage(btnView.clicks())   
        }
    
        override fun render(state: MyState) {
            myTextView.text = state.message
        }
    }
```

### Sample App
You can find a sample app over [here]().

### Contributing
If you like this project, or are using it in your app, consider starring the repository to show your support. 
Contributions from the community are very welcome.
