# KeyBoi
A simple padlock and key system.

## How to use
Using KeyBoi is straight-forward and simple.

KeyBoi consists of two parts: a padlock and a key.

A **padlock** is a special sign that a player may attach to a lockable block. Lockable blocks include chests, barrels, doors (wood and iron), trapdoors (wood and iron), and gates. Each padlock supports only one key at a time.

A **key** is an item that can unlock the padlock. Any item or block may become a key. Keys may be used to open many doors, chests, etc.

### Creating a key
1. Place the item you want to make into a key in your main hand.
2. In chat, type and run "/key create" without the quotes.
3. Your new keys should now have added lore stating it is a key.

**Note:** When you create a key, the entire stack of items will become keys. For example, if you have a stack of 32 sticks in your hand, all 32 sticks will become identical keys.

### Creating a padlock
1. Attach a sign to a lockable block.
2. On the first line, type "[key]" without the quotes.
3. Place a key in your main hand and use it (right-click) on the padlock sign.

### Changing keys on a padlock
1. Remove the current padlock sign.
2. Follow the steps to make a new padlock sign.

### Removing key information from an item
If you decide to remove key data from an item, hold the key in your main hand and type and run "/key remove". If the key you are holding was made by someone else, you will get a warning message and have to type the command again. Be careful, this process cannot be undone!

## List of commands
* /key - show all KeyBoi commands
* /key create - creates a stack of items into keys
* /key remove - removes key data from a key item
* /key tutorial - shows steps on how to use KeyBoi

## Current known issues
1. Lockable blocks may only have one usable padlock at any given time. If you were to place a second or third padlock on the block, the key it will expect may be unknown.
2. Opening iron doors and iron trapdoors with a written book key may open the book itself, but the door/trapdoor will still open.
