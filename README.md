# gatekeeper

Gatekeeper is a highly-available, distributed and secure SSL-termination manager.

> **Note:** Until version 1.0.0 is reached, gatekeeper should be considered unstable and unready for production use.

## Configuration

A configuration file must be valid YAML, and can contain any of the following configuration directives.

```yaml
# Configure high-availability.
replication:

  # The local address that gatekeeper will bind to for peer-to-peer communication.
  address: 127.0.0.1

  # The local port that gatekeeper will use for peer-to-peer communication.
  port: 7123

  # A location to store persistent replication data.
  # This directory must be readable and writeable by the user gatekeeper is running under.
  directory: /var/lib/gatekeeper

  # Set this to true if this node is eligible to be a cluster leader.
  # At least one node in the cluster must have this set, and it is recommended to have 3.
  server: true

```