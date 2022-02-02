# kotlin-coroutines-rx

搭建 RxJava1 和 kotlin coroutine 之间的桥梁.

## 特性

### Observable

| 函数名                     | 描述                                                               |
| -------------------------- | ------------------------------------------------------------------ |
| `Observable.awaitFirst`    | 获取 `Observable` 的第一个值                                       |
| `Observable.awaitLast`     | 获取 `Observable` 的最后一个值                                     |
| `Observable.awaitSingle`   | 获取 `Observable` 的单个值(当 `Observable` 发射多个值时会抛出异常) |
| `Observable.awaitComplete` | 等待 `Observable` 完成                                             |
| `Observable.asFlow`        | 通过 `Observable` 创建 `Flow`                                      |
| `Flow.asObservable`        | 通过 `Flow` 创建 `Observable`                                      |
| `rxObservable`             | 通过 `ProducerScope` 创建 `Observable`                             |

### Single

| 函数名                 | 描述                                 |
| ---------------------- | ------------------------------------ |
| `Single.awaitSingle`   | 获取 `Single` 的结果                 |
| `Single.awaitComplete` | 等待 `Single` 结束                   |
| `rxSingle`             | 通过 `suspend` 类型函数创建 `Single` |

### Completable

| 函数名                      | 描述                                      |
| --------------------------- | ----------------------------------------- |
| `Completable.awaitComplete` | 等待 `Completable` 完成                   |
| `rxCompletable`             | 通过 `suspend` 类型函数创建 `Completable` |

### Schedule

| 函数名                           | 描述                                       |
| -------------------------------- | ------------------------------------------ |
| `Schedule.asCoroutineDispatcher` | 通过 `Schedule` 创建 `CoroutineDispatcher` |

## 许可证

```
MIT License

Copyright (c) 2022 ChenRenJie

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
