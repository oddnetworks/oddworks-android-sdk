package io.oddworks.device.request


/**
 * Specify the type of OddResource to return on success.
 *
 * <p>When fetching a set of potentially mixed resources,
 * use {@code LinkedHashSet<OddResource>}
 *
 * @param  type of {@link OddResource} returned on success
 */
interface OddCallback<T> {
    fun onSuccess(resource: T)
    fun onFailure(exception: Throwable)
}