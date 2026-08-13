# Contributing to Yggy

Short version: **we cannot accept pull requests, and we do want your bug reports.**

## How this repository works

Yggy is developed in a private repository. This public repository is a **read-only
mirror** of it, republished as the project progresses. Because every sync replaces
the published history, a commit pushed here would be overwritten by the next
release — there is no path for an external commit to reach the product.

That is a constraint of how the project is released, not a judgement on the code
anyone might offer.

## Pull requests

**Not accepted.** Please do not spend time preparing one; it will be closed without
review, and we would rather tell you that before you write it than after.

If you have found a bug and you already know the fix, open an issue and describe
the fix in it — a clear explanation, or a diff pasted into the issue, is genuinely
useful and we can apply it internally with credit.

## Bug reports

**Welcome, and triaged by the team.** Open one at
[github.com/AmadeusITGroup/Yggy/issues](https://github.com/AmadeusITGroup/Yggy/issues).

A report we can act on usually includes:

- what you expected to happen, and what happened instead
- the smallest set of steps that reproduces it
- the affected module (for example `aud/libraries/lib-models`) and the commit or
  release you are on
- your Java and Maven versions, and your OS, for anything build- or run-related
- the relevant log output or stack trace as text rather than a screenshot

If you are not sure it is a bug, open the issue anyway and say so.

## Security vulnerabilities

**Do not open a public issue.** Report privately by email to
`opensource@amadeus.com`, as described in [SECURITY.md](./SECURITY.md). You can
expect an acknowledgement within 5 business days.

## Feature requests and questions

You are welcome to open an issue for either. Be aware that the roadmap is driven by
internal requirements, so we may not be able to commit to a request or to a response
time.

## Code of conduct

Everything above happens under our [Code of Conduct](./CODE_OF_CONDUCT.md).

## What you can do with this code

Being closed to pull requests says nothing about your rights to the code. Yggy is
licensed under the [Apache License 2.0](./LICENSE), which lets you use, modify,
distribute and build on it, including commercially, subject to the terms of the
licence. Forking it and taking it in your own direction is entirely legitimate — we
simply cannot merge that work back.
