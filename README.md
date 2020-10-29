# FU Mail App README

<span style="color:red">DISCLAMER: THIS PROJECT IS IN NO WAY AFFILLIATED WITHE THE ACTUAL FREIE UNIVERSITÄT BERLIN </span>

This is a <i>in devleopment</i> Mail Client that allows you to write and receive Mails from the ZeDat UNIX Webmail of the Freie Universität Berlin.

This project is freezed for now, until I am more relaxed

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
> Tip: You can also embed HTML into your Mails and open the app with any Variation of the [mailto:](https://en.wikipedia.org/wiki/Mailto#Examples) Link

### Add a signature to your mail ✔️

Use your University Mail for work at the FU? Add a (HTML) Signature so people know how to reach/find you

### New Mail Widget ❌

A custom Widget to see new Mails directly on your Home Screen

## Known Issues

- Mails may be marked as Spam (Currently only on web.de)
- If user chooses not to save Login Data. It may sometimes still be saved (This will be fixed when I cleanup my Code after the App works)
- It is possible to login with wrong credentials. Sending Mails will obviously not work

## Release Notes
### ALPHA 0.8.0
- UI added
- Login added
- Settings added
- Mail sending added
- Signature added
- Attachments fully added :clap: :clap: :clap: :clap:
