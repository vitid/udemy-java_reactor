* Use ```Mono.empty()``` to return 0 element
* Use ```Mono.error()``` to throw out an error
* If you use ```Mono.just(function(...))```, whatever inside ```function(...)``` will be executed immediately. That's not correct. It shouldn't be executed until someone subscribe to this mono. Use ```Mono.fromSupplier(() -> function(...)``` or ```Mono.fromCallable(() -> function(...))```instead
* By default, publisher will use the main thread when performing the operation. So doing something like this will be blocked on the main thread 
```
    main(...){
        function(...)
        function(...).subscribe()
        
        //the third function call will be blocked
        function(...)
    }
    
    //return Mono publisher
    Mono<...> function(...)
    {
        //long execution
    }
```
You need to subscribe the 2nd publisher on another thread, like:
```
    function(...)
    function(...)
           .subscribeOn(Schedulers.boundedElastic())
           
    //the third function call not blocked now
    function(...)
```
* We can create ```Mono``` from ```Future```(when you need to return something) or ```Runnable```(when return nothing). If it's from ```Runnable``` then that means ```onNext()``` will not be called
* Use ```Mono<Void>``` with ```Mono.fromRunnable()```
* Use ```then(Mono)``` to provide as complete signal. With ```then()```, this will return ```Mono<Void>```
* Since java ```Stream``` can be used only once. If you subscribe to ```Flux.fromStream(${streamObject})``` multiple times will cause an error. The proper way to do it is using the supplier version```Flux.fromStream(() -> {${generate a new stream}})```
* Use ```.log()``` on ```Mono/Flux``` to make sense of the operation that's going on
* You can implement a custom ```Subscriber``` with ```subscribeWith()```to have a better control of how subscription should work. 
You use ```Subscription``` object to request/cancel the subscription. Unlike ```subscribe()```, this will not emit object after you subscribe. Note that if the subscription is canceled, ```onComplete()``` will also not be invoked. Refer to memo ```CustomSubscriber``` and ```StockObserver```. Also note the difference between ```CustomSubscriber``` and ```StockObserver```, in ```CustomSubscriber``` is run on the main thread while ```StockObserver``` is run on a separate thread
* Use ```Flux.next()``` to convert a flux to a mono
* Use ```Flux.create() / push() / generate()``` to create your own publisher. Refer to ```FluxSinkCreateGeneratePush```
  * ```Flux.crate()``` need to handle downstream cancellation. Can create ```Consumer``` in its own separated class and be shared by another thread in a Thread-safe way
  * ```Flux.push()``` is similar to ```Flux.create()``` but is not Thread-safe
  * For ```Flux.generate(...)```, it'll create a new instance of consumer everytime subscriber make a new request. It doesn't need to handle downstream cancellation and can emit only one element (because the next time subscriber make a request, a new Consumer instance will be created). This come with 3 variations:
    * ```Flux.generate(...1 parameter...)```: this's similar to ```Flux.create()```
    * ```Flux.generate(...2 parameters...)```: can be used to pass on state from earlier run
    * ```Flux.generate(...3 parameters...)```: can be used to pass on state from earlier run. The last argument is of type ```Consumer<S>``` to handle the state. Useful for closing related resources
* Operator (see ```Operator```):
  * ```handle()``` use to perform map, filter and can use to send the complete signal    
  * Various callback operator can be used as a hook during subscribe, next, complete, cancel, discard, etc.
  * ```limitRate()``` limit the rate of how many data to send to downstream. We can also define replenish rate (default to 75%)
  * ```delayElements()``` this will delay and run in a separate thread. Internally, it has mechanism similar to ```limitRate()```. By default, it'll request 32 elements from upstream initially(there's no need to request unbound element since it'll apply delay)
  * ```onErrorReturnOperator()``` return fallback value. Operation will be terminated after the error is emitted
  * ```onErrorResumeOperator()``` we can use this one to switch to fallback publisher instead. Operation will be terminated after the error is emitted
  * ```onErrorContinue()``` unlike the above two, this one can be used to capture the source of the error and continue the operation
  * ```timeout()``` can be used to switch to the fallback publisher after the upstream is timed out
  * ```defaultIfEmpty()``` produce a value if upstream produce nothing
  * ```switchIfEmpty()``` use this to switch to fallback publisher instead
  * ```transform()``` this is for using with custom operation functions that can be reused
  * ```switchOnFirst()``` switch to the 2nd publisher if the **FIRST** vaue emitted follow some condition
  * ```flatMap()``` very useful when we have one service that return Publisher, which call another service that return another Publisher. We use this to flatten the 2nd publisher result. **Note** the result can be out-of-order (better look at the memo code)
  * ```concatMap()``` fix the above ```flatMap()``` issue. It'll drain element from the 1st publisher before going to subsequent publisher
* What we have use so far is **Cold** Publisher. It'll emit a new item for each subscriber
* **Hot** Publisher can be shared by multiple subscribers. See ```HotColdPublisher```
    * ```share()``` can be used to convert from Cold to Hot Publisher. This's the same as calling ```publish().refCount(1)```. We can set ```refCount(count)``` to set the minimum number of subscribers required before Hot Publisher start producing. **Note** that after it emit all items and there's a new subscribers that are subscribed (equal to the number of count), this Hot Publisher will start acting like a Cold Publisher where it'll produce the element again
    * To prevent Hot Publisher from producing element again, as mentioned in the above, use ```autoConnect(count)``` instead. If you set count to 0, it'll start emitting element immediately, even with no subscriber (not really see usecase in this)
    * Use ```cache(history_count)``` to cache the history of Hot Publisher so the future subscribers can be do on ```onNext()``` immediately
* Threading & Schedulers (refer ```ThreadingSchedule```)
    * Use ```Schedulers.boundedElastic()``` for IO task and ```Schedulers.parallel()``` for CPU intensive task. ```Schedulers.parallel()``` **doesn't** mean that the value will be emitted in parallel
    * ```subscribeOn()``` is for Publisher. If you develop a Publisher, you should specify ```subscribeOn()``` to it. If there're many ```subscribeOn()```, the one closest to the source take the priority
    * ```publishOn()``` is for Subscriber. Thread will be swtiched as it flow down from the source to subscriber
    * To make value emitted from Publisher being done in a parallel way, use ```parallel() & runOn()```
    * Operation ```interval(), delayElements()``` internally subscribe to a separate thread
* Backpressure (refer ```BackPressure```)
    * ```onBackpressureBuffer()``` is a dafault behavior. It'll hold all emitted values in memory
    * You can use ```onBackpressureDrop/Latest/Error()``` to handle overflow starategy. There's also a variant that let you define Consumer to manage the dropped element(can be usefule like putting the dropped element into a file for later process)
* CombinePublishers (refer ```CombinePublishers```)
    * ```A.startWith(B)```: B will be completely drained before going to A. Can be used as a caching pattern
    * ```A.concatWith(B)```: A will be completely drained before going to B. Use ```concatDelayError()``` to delay the error
    * ```A.merge(B)```: emit as each publishers emitting separately
    * ```A.zip(B)```: zip operation
    * ```A.combineLatest(B)```: combine the **latest** value of A & B. Useful for something like stock price
* Batching (refer ```Batching```)
    * ```buffer(n)``` group incoming elements in to a list of n elements. Can also supply ```Duration``` to emit the list of value as soon as time out reach or use both hybrid approach. Can use with ```skip()``` to keep some number of the last elements in the group (if skip < n) or do sampling (if skip > n)
    * ```window()``` this is similar to ```buffer()``` but it'll create a flux insterad of List. The benefit is it can be process immediately. When the current group is done processing, it'll send ```onComplete``` signal and create a new ```Flux```
    * ```groupBy()``` to group fluxes by a key. This returnts ```GroupedFlux```. Need to do another subscribe to process that group
* Repeat and Retry (refer ```RepeatRetry```)
    * ```repeat()``` to repeat the publisher (in case there's no error). There's a version that can be supplied with terminate condition. **Noted** that the subscriber will not receive the complete signal until the last repeat
    * ```retry()``` to retry in case of error. Can use ```Retry.fixedDelay()``` etc to add delay and more advance options. ```Retry.from(...)``` to customize the retry flow to do something like: *keep retrying if server error is 5xx but stop if it's 4xx*
* Sink (refer ```Sink```)
    * Instead of using facory methos in ```Mono.xxx()``` or ```Flux.xxx()``` to initiate the message publisher, we can publish the message directly using ```Sinks```. This can be converted to either ```Mono``` (to emit only 1 message) or ```Flux```. Then use ```Sinks``` object to publsh a message and ```Mono / Flux``` object to receive the message
    * ```Sinks``` can be either unicast (limit to only 1 subscriber) or multicast. Behavior can be configured to behave like Hot publisher or Cold publisher (keep some message history)
* Context (refer ```ContextDemo```)
    * Subscriber can send a context to the Publisher. Context is just a key-value pair
    * Use ```contextWrite()``` on the downstream to set a context from downstream to upstream. The value can be overridden
    * Use ```deferContextual()``` on the upstream to obtain Context from the downstream
* Unittest using ```StepVerifier``` (refer: ```UnitTest```)
    * ```StepVerifier.create``` will subscribe to the publisher for you. It will also perform blocking so you don't need to block the thread yourself
    * ```expectNext(), assertNext(), expectNextCount(), verifyError()``` and their variants can be used
    * ```verify(Duration)``` can be used if you want to wait for only a certain amount of time
    * Use ```withVirtualTime()``` to speed up the test. You can also use ```expectNoEvent(Duration)``` in case you expect no event in that duration. **Note** that you should have ```expectSubscription()``` first
    * Test can be annotated with scenario name using ```StepVerifierOptions.create().scenarioName()``` or ```as()```
    * Use ```StepVerifierOptions.create().withInitialContext()``` to inject initial context for the test
