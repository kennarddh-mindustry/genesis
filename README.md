# Genesis

- General Purpose Mindustry Plugin Library
- This plugin removes mindustry default kick, ban, vote kick, admin, system

> [!IMPORTANT]
> Genesis override some mindustry code using reflection. This library is probably will not be compatible
> with other mindustry plugins.

> [!IMPORTANT]
> It won't be compatible if the plugin register commands, use `handleServer`, etc.
> The behaviour will be unknown because Genesis manage some internal states

## Todo

- [x] Optional Command Parameter
- [x] Event Handler
- [x] Better Filter System With Priority
- [x] Timer task annotation
- [x] Command Usage. Auto generated.
- [x] Command with more than 1 name/aliases
- [x] Separate core with common. Common will include like host command.
- [ ] Publish artifact
- [x] Event on command changed
- [x] Foo support
- [x] Command description
- [x] (common) Help command
- [x] Remove command
- [x] Enum parameter support
- [x] Handle server annotation for `net.handleServer`
- [ ] Command validation with player as parameter for the validator function
- [ ] Player as parameter. Need converter. Prioritize id then search for name.