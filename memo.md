* Use ```Mono.empty()``` to return 0 element
* Use ```Mono.error(...)``` to throw out an error
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
* Use ```Mono<Void>``` with ```Mono.fromRunnable(...)```
* Since java ```Stream``` can be used only once. If you subscribe to ```Flux.fromStream(${streamObject})``` multiple times will cause an error. The proper way to do it is using the supplier version```Flux.fromStream(() -> {${generate a new stream}})```
* Use ```.log()``` on ```Mono/Flux``` to make sense of the operation that's going on
* You can implement a custom ```Subscriber``` with ```.subscribeWith(...)```to have a better control of how subscription should work. 
You use ```Subscription``` object to request/cancel the subscription. Unlike ```.subscribe(...)```, this will not emit object after you subscribe. Note that if the subscription is canceled, ```onComplete()``` will also not be invoked. Refer to memo ```CustomSubscriber``` and ```StockObserver```. Also note the difference between ```CustomSubscriber``` and ```StockObserver```, in ```CustomSubscriber``` is run on the main thread while ```StockObserver``` is run on a separate thread
* ```Flux.interval(...)``` is run on a separate scheduler by default
* Use ```Flux.next()``` to convert a flux to a mono
* Use ```Flux.create() / push() / generate()``` to create your own publisher. Refer to ```FluxSinkCreateGeneratePush```
  * ```Flux.crate(...)``` need to handle downstream cancellation. Can create ```Consumer``` in its own separated class and be shared by another thread in a Thread-safe way
  * ```Flux.push(...)``` is similar to ```Flux.create(...)``` but is not Thread-safe
  * For ```Flux.generate(...)```, it'll create a new instance of consumer everytime subscriber make a new request. It doesn't need to handle downstream cancellation and can emit only one element (because the next time subscriber make a request, a new Consumer instance will be created). This come with 3 variations:
    * ```Flux.generate(...1 parameter...)```: this's similar to ```Flux.create(...)```
    * ```Flux.generate(...2 parameters...)```: can be used to pass on state from earlier run
    * ```Flux.generate(...3 parameters...)```: can be used to pass on state from earlier run. The last argument is of type ```Consumer<S>``` to handle the state. Useful for closing related resources