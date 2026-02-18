# MailSystem-JavaFX

## Team
- Denis Budau

## Project Overview
Distributed mail system consisting of two JavaFX applications:
1. **Mail Server**: manages mailboxes and messages for registered users
2. **Mail Client**: allows users to read, send, reply, forward emails

The client and server communicate only via **textual data over Java sockets**, following MVC and Observer patterns.  

## Architecture Overview

### Mail Server
- Stores mailbox data persistently in files
- Handles 3 pre-defined users
- Logs client interactions (connections, message sending, errors)
- GUI displays server logs

### Mail Client
- Requires email address as identifier
- Maintains inbox locally
- GUI allows reading, sending, replying, forwarding emails
- Shows connection status with server
- Validates emails using Regex
- Partially responsive with automatic inbox updates

## Technologies
- Java 17+
- JavaFX
- MVC + Observer pattern
- Sockets for communication
- File persistence

## Notes
- Supports multiple users
- Designed for scalability
- Error handling for server disconnection and invalid addresses

This project was developed as part of a university course assignment at the University of Turin (Informatics Bachelor's, 2024-25).  
It was completed and then uploaded to GitHub for portfolio purposes.
