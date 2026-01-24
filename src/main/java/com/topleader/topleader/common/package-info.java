/**
 * Common Module - Shared Kernel
 *
 * This module contains shared utilities and infrastructure code that can be used by all other modules.
 * It should NOT depend on any business modules (coach, user, session, etc.).
 *
 * Provided utilities:
 * - AI integrations (OpenAI client)
 * - Calendar integrations (Google Calendar, Calendly)
 * - Email services (templates, sending)
 * - Exception handling
 * - Notification system
 * - Password management
 * - Common utilities (JSON, images, transactions, etc.)
 *
 * Architecture rule: Common is a SHARED KERNEL - all modules can use it, but it should NOT depend on them.
 *
 * Note: Spring Modulith annotations are defined in test configuration only.
 */
package com.topleader.topleader.common;
