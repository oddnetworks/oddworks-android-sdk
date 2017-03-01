# About Oddworks

Oddworks is, at its core, a Video Management System (VMS). It provides a central location to organize media and configure the devices that serve it.

# Oddworks Device SDK

At a high level, this SDK wraps the Oddworks API with an OkHttp client, parsing the [JSON API format](http://jsonapi.org/format/) into helpful `OddResource` objects.

## Oddworks Resources

Oddworks has a handful of specific resource types that are used in concert to construct media applications. No matter which Online Video Provider (OVP) is used to provide media to Oddworks, the data will be massaged into one of these standardized objects. Our thought is that consumer applications should not need to change when adding a new OVP or changing where a video is stored.

### Resource Types

#### config

The `config` type will contain device-specific configuration data.

### video

The `video` resource is the main media object that is the meat and potatoes Odd Networks.

### promotion

The `promotion` resource type is an object that is essentially the same as a `video` without the stream. Essentially just image or text content.

### collection

A `collection` resource is an object that contains other resources via its `entities` and `featured` relationships.

### view

A `view` resource is an object that can contain other entities through user-defined custom relationships.


## Data Classes

The SDKs data classes are modeled around parsing JSON API schema served by Oddworks. For more information about the REST API data models, visit [oddnetworks.com/docs/resources](https://oddnetworks.com/docs/resources/).

### OddIdentifier

This is the base class of an `OddResource`. It contains a resource's `id` and `type`. It is a separate class so that we can pass around JSON API identifier objects in Java form.

### OddResource

The `OddResource` class extends `OddIdentifier` and is a superclass of many other data models in the SDK. It's children include `OddView`, `OddCollection`, `OddVideo`, `OddConfig`, `OddViewer`, and `OddPromotion`. It provides many convenience methods that help you sort through data received from the API such as `getRelationship`, `getIncludedByRelationship`, etc. These getter methods can return any combination of `OddResource`'s subclass objects depending on how you've configured your data in the Oddworks dashboard.


### OddView

The `OddView` class makes it possible to retrieve and store large amounts of data from the API with a single call. This view contains "relationships" and "included" pieces of data that can be accessed via `OddResource`'s convenience methods. When constructing applications you may find it helpful to package things such as your navigation and the initial data shown on application load into an `OddView`.

### OddCollection

The `OddCollection` class makes it possible to group together other entities for explicit display. Depending on how you configure your Collection on the Oddworks dashboard it can contain a description, images, a release date, a title, an ID, a related `OddIdentifier` objects, relationships, included `OddResource`s, and a type.


### OddVideo

The `OddVideo` class can represent any type of playable media to users. This could be a VOD, HLS Stream, DASH Stream, audio file, etc. Depending on how you configure your OddVideo on the Oddworks dashboard it can contain a description, a duration, images, a release date, a title, stream data, an ID, a related `OddIdentifier` object, relationships, included `OddResource`s (such as related videos), and a type.

### OddPromotion

The `OddPromotion` class is used to handle any type of non-media content you may want to put in the application. Depending on how you configure your OddPromotion on the Oddworks dashboard it can contain a description, a title, images, a related `OddIdentifier` object, relationships, included `OddResource`s, and a type.