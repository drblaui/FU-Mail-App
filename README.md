# FU Mail App README

<span style="color:red">DISCLAMER: THIS PROJECT IS IN NO WAY AFFILLIATED WITHE THE ACTUAL FREIE UNIVERSITÄT BERLIN </span>

This is a <i>in devleopment</i> Mail Client that allows you to write and receive Mails from the ZeDat UNIX Webmail of the Freie Universität Berlin.

## Features
> Impelemted: ✔️ Planned but not yet implemented: ❌


### Save your Login data encrypted on your device ✔️

By using the Android-integrated Keystore Algorithms your Login Data is saved LOCALLY and encrypted on you device neither me or anyone else knows how to decypher your password except your Phone. You can disable this at any time.

### Choose your own name ✔️

Since I don't have access to any offical ZeDat Database (I also don't want access to one), you have the ability to change your Username to whatever you want. I do reccomend to use you actual name to get taken serious though.


### View your mails ❌

With the help of JavaMail you can view all folders that are in the UNIX Mebmail. That means `INBOX`, `Drafts`, `Trash`, `Sent`, `Spam` and even a new one `Unread`.

### Write Mails to anyone ✔️

Just like every other Mail Client, this App allows you to write Mails to anybody with a real Mail Address, doesn't matter if it's from the FU or from somewhere else
> While sending Mails is implemented and a Button for the Action is also there, you still can't add attachments

> Tip: You can also embed HTML into your Mails and open the app with any Variation of the [mailto:](https://en.wikipedia.org/wiki/Mailto#Examples) Link

### Add a signature to your mail ❌

Use your University Mail for work on the FU? Add a Signature so people know how to reach/find you


## Known Issues

Mails may be marked as Spam

## Release Notes
### INDEV 0.4.0
- UI added
- Login added
- Settings added
- Mail sending added
